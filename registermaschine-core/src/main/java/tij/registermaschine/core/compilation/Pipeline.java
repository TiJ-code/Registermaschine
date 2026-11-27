package tij.registermaschine.core.compilation;

import tij.registermaschine.api.compilation.ICompiler;
import tij.registermaschine.api.compilation.ILexer;
import tij.registermaschine.api.compilation.IParser;
import tij.registermaschine.api.compilation.compiling.ICompiledInstructionFactoryAware;
import tij.registermaschine.api.compilation.compiling.ICompiledProgram;
import tij.registermaschine.api.compilation.lexing.IToken;
import tij.registermaschine.api.compilation.lexing.ITokenFactoryAware;
import tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import tij.registermaschine.api.error.DefectPipelineException;
import tij.registermaschine.api.error.SyntaxErrorException;
import tij.registermaschine.api.instructions.IInstructionSet;
import tij.registermaschine.api.log.ILogger;
import tij.registermaschine.api.log.LoggerFactory;
import tij.registermaschine.api.registry.IFactoryRegistry;
import tij.registermaschine.core.Components;

import java.util.List;
import java.util.Objects;

/**
 * Compiles source code through lexer, parser and compiler factories in the component registries.
 * Initialise core with {@link tij.registermaschine.core.CoreStartup#init} before using the
 * default selection. Custom factories can be selected by ID for each compilation.
 * Stage factories should create fresh instances, since factory-aware stages are configured
 * for each compilation and may otherwise be shared concurrently.
 *
 * @author TiJ
 * @since 1.0.0
 */
public final class Pipeline {
    /**
     * Logger for pipeline stage execution and component creation.
     */
    private static final ILogger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    /**
     * Instruction set used by the global convenience methods, or null until configured.
     */
    private static volatile IInstructionSet globalInstructionSet;

    /**
     * Prevents instantiation of this utility class.
     */
    private Pipeline() {
    }

    /**
     * Sets the instruction set used by the global convenience methods.
     *
     * @param set instruction set to use
     * @throws NullPointerException if the set is null
     */
    public static void setGlobalInstructionSet(IInstructionSet set) {
        globalInstructionSet = Objects.requireNonNull(set, "InstructionSet cannot be null");
    }

    /**
     * Compiles with the global instruction set and default selection.
     *
     * @param sourceCode source to compile
     * @return the compiled program
     * @throws NullPointerException    if no global instruction set has been set
     * @throws SyntaxErrorException    if parsing fails
     * @throws DefectPipelineException if a stage cannot be created
     */
    public static ICompiledProgram compileWithGlobal(String sourceCode) {
        return compile(sourceCode, globalInstructionSet);
    }

    /**
     * Compiles with the default selection.
     *
     * @param sourceCode source to compile
     * @param set        instruction set to use
     * @return the compiled program
     * @throws NullPointerException    if the instruction set is null
     * @throws SyntaxErrorException    if parsing fails
     * @throws DefectPipelineException if a stage cannot be created
     */
    public static ICompiledProgram compile(String sourceCode, IInstructionSet set)
            throws SyntaxErrorException, DefectPipelineException {
        return compile(sourceCode, set, Selection.DEFAULT);
    }

    /**
     * Compiles with the selected registry factories.
     *
     * @param sourceCode source to compile
     * @param set        instruction set to use
     * @param selection  factory IDs for the stages
     * @return the compiled program
     * @throws NullPointerException    if the instruction set or selection is null
     * @throws SyntaxErrorException    if parsing fails
     * @throws DefectPipelineException if a stage cannot be created
     */
    public static ICompiledProgram compile(String sourceCode, IInstructionSet set,
                                           Selection selection)
            throws SyntaxErrorException, DefectPipelineException {
        return tokenize(sourceCode, set, selection).parse().compile();
    }

    /**
     * Tokenizes with the global instruction set and default selection.
     *
     * @param sourceCode source to tokenize
     * @return the token stage
     * @throws NullPointerException    if no global instruction set has been set
     * @throws DefectPipelineException if the lexer cannot be created
     */
    public static TokenStage tokenizeWithGlobal(String sourceCode) {
        return tokenize(sourceCode, globalInstructionSet);
    }

    /**
     * Tokenizes with the default selection.
     *
     * @param sourceCode source to tokenize
     * @param set        instruction set to use
     * @return the token stage
     * @throws NullPointerException    if the instruction set is null
     * @throws DefectPipelineException if the lexer cannot be created
     */
    public static TokenStage tokenize(String sourceCode, IInstructionSet set) {
        return tokenize(sourceCode, set, Selection.DEFAULT);
    }

    /**
     * Tokenizes with the selected lexer and retains the selection for subsequent stages.
     *
     * @param sourceCode source to tokenize
     * @param set        instruction set to use
     * @param selection  factory IDs for the stages
     * @return the token stage
     * @throws NullPointerException    if the instruction set or selection is null
     * @throws DefectPipelineException if the lexer or token factory cannot be created,
     *                                 or the lexer does not support the selected token factory
     */
    public static TokenStage tokenize(String sourceCode, IInstructionSet set,
                                      Selection selection) {
        Objects.requireNonNull(set, "InstructionSet cannot be null");
        Objects.requireNonNull(selection, "Selection cannot be null");

        LOGGER.debug("Tokenizing source with lexer: {}", selection.lexer());
        ILexer lexer = create(Components.REGISTRIES.lexers(), selection.lexer());
        if (lexer instanceof ITokenFactoryAware aware) {
            aware.setTokenFactory(create(Components.REGISTRIES.tokens(), selection.tokenFactory()));
        } else if (!Components.DEFAULT.equals(selection.tokenFactory())) {
            throw new DefectPipelineException(
                    "Selected lexer does not support a custom token factory: " + selection.lexer(), null);
        }
        return new TokenStage(lexer.tokenize(sourceCode, set), set, selection);
    }

    /**
     * Creates a pipeline component from its registry, wrapping factory failures.
     *
     * @param registry registry containing the component factory
     * @param id       factory ID
     * @param <T>      component type
     * @return the component created by the factory
     * @throws DefectPipelineException if the registry or factory fails to create the component
     */
    private static <T> T create(IFactoryRegistry<T> registry, String id) {
        try {
            LOGGER.trace("Creating pipeline component: {}", id);
            return registry.create(id);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to create pipeline component: {}", id, e);
            throw new DefectPipelineException("Failed to create pipeline component: " + id, e);
        }
    }

    /**
     * Registry IDs for the stages of a compilation.
     *
     * @param lexer                      lexer factory ID
     * @param parser                     parser factory ID
     * @param compiler                   compiler factory ID
     * @param tokenFactory               token factory ID
     * @param compiledInstructionFactory compiled instruction factory ID
     * @author TiJ
     * @since 2.0.0
     */
    public record Selection(
            String lexer,
            String parser,
            String compiler,
            String tokenFactory,
            String compiledInstructionFactory
    ) {
        /**
         * Selection using the core default factory for each stage.
         */
        public static final Selection DEFAULT = new Selection(Components.DEFAULT);

        /**
         * @throws NullPointerException if any ID is null
         */
        public Selection {
            Objects.requireNonNull(lexer, "lexer");
            Objects.requireNonNull(parser, "parser");
            Objects.requireNonNull(compiler, "compiler");
            Objects.requireNonNull(tokenFactory, "tokenFactory");
            Objects.requireNonNull(compiledInstructionFactory, "compiledInstructionFactory");
        }

        /**
         * Uses the same ID for all stage and product factories.
         *
         * @param globalId ID to use for every registry
         */
        public Selection(String globalId) {
            this(globalId, globalId, globalId, globalId, globalId);
        }

        /**
         * Selects stage factories while using default product factories.
         *
         * @param lexer    lexer factory ID
         * @param parser   parser factory ID
         * @param compiler compiler factory ID
         */
        public Selection(String lexer, String parser, String compiler) {
            this(lexer, parser, compiler, Components.DEFAULT, Components.DEFAULT);
        }
    }

    /**
     * Tokenization result and context for parsing.
     *
     * @param tokens    tokens produced by the lexer
     * @param set       instruction set used for compilation
     * @param selection factory IDs for the stages
     */
    public record TokenStage(List<IToken> tokens, IInstructionSet set, Selection selection) {
        /**
         * Parses the tokens with the selected parser.
         *
         * @return the parse stage
         * @throws SyntaxErrorException    if parsing fails
         * @throws DefectPipelineException if the parser cannot be created
         */
        public ParseStage parse() throws SyntaxErrorException {
            LOGGER.debug("Parsing tokens with parser: {}", selection.parser());
            IParser parser = create(Components.REGISTRIES.parsers(), selection.parser());
            return new ParseStage(parser.parse(tokens), set, selection);
        }
    }

    /**
     * Parsing result and context for compilation.
     *
     * @param syntaxTree syntax tree produced by the parser
     * @param set        instruction set used for compilation
     * @param selection  factory IDs for the stages
     */
    public record ParseStage(ISyntaxTree syntaxTree, IInstructionSet set, Selection selection) {
        /**
         * Compiles the syntax tree with the selected compiler.
         *
         * @return the compiled program
         * @throws DefectPipelineException if the compiler or instruction factory cannot be created,
         *                                 or the compiler does not support the selected instruction factory
         */
        public ICompiledProgram compile() {
            LOGGER.debug("Compiling syntax tree with compiler: {}", selection.compiler());
            ICompiler compiler = create(Components.REGISTRIES.compilers(), selection.compiler());
            if (compiler instanceof ICompiledInstructionFactoryAware aware) {
                aware.setCompiledInstructionFactory(
                        create(
                                Components.REGISTRIES.compiledInstructions(),
                                selection.compiledInstructionFactory()
                        )
                );
            } else if (!Components.DEFAULT.equals(selection.compiledInstructionFactory())) {
                throw new DefectPipelineException(
                        "Selected compiler does not support a custom instruction factory: " + selection.compiler(), null);
            }
            return compiler.compile(syntaxTree, set);
        }
    }
}
