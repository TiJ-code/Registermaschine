package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.api.compilation.ICompiler;
import dk.tij.registermaschine.api.compilation.ILexer;
import dk.tij.registermaschine.api.compilation.IParser;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledProgram;
import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.error.DefectPipelineException;
import dk.tij.registermaschine.api.error.SyntaxErrorException;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.List;
import java.util.Objects;

/**
 * Central compilation pipeline for the Registermaschine.
 *
 * <p>This class provides a configurable pipeline to transform source code into
 * a compiled program via three stages: tokenisation, parsing, and compilation.
 * Custom {@link ILexer lexer}, {@link IParser parser}, and {@link ICompiler compiler}
 * implementations can be used globally.</p>
 *
 * <p>The pipeline supports a global {@link IInstructionSet} or a per-invoke
 * instruction set for compilation.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class Pipeline {
    private Pipeline() {}

    private static Class<? extends ILexer> pipelineLexer = ConcreteLexer.class;
    private static Class<? extends IParser> pipelineParser = ConcreteParser.class;
    private static Class<? extends ICompiler> pipelineCompiler = ConcreteCompiler.class;
    private static IInstructionSet globalInstructionSet;

    /**
     * Sets a custom lexer for the pipeline.
     *
     * @param customLexer the lexer class to use
     */
    public static void useLexer(Class<? extends ILexer> customLexer) {
        Objects.requireNonNull(customLexer, "Custom Lexer cannot be null");
        pipelineLexer = customLexer;
    }

    /**
     * Sets a custom parser for the pipeline
     *
     * @param customParser the parser class to use
     */
    public static void useParser(Class<? extends IParser> customParser) {
        Objects.requireNonNull(customParser, "Custom Parser cannot be null");
        pipelineParser = customParser;
    }

    /**
     * Sets custom compiler for the pipeline
     *
     * @param customCompiler the compiler class to use
     */
    public static void useCompiler(Class<? extends ICompiler> customCompiler) {
        Objects.requireNonNull(customCompiler, "Custom Compiler cannot be null");
        pipelineCompiler = customCompiler;
    }

    /**
     * Sets the global {@link IInstructionSet} used by pipeline methods ending in `WithGlobal`.
     *
     * @param set the global instruction set
     */
    public static void setGlobalInstructionSet(IInstructionSet set) {
        Objects.requireNonNull(set, "Custom global InstructionSet cannot be null");
        globalInstructionSet = set;
    }

    /**
     * Compiles source code using the global instruction set.
     *
     * @param sourceCode the source code to compile
     * @return the compiled program
     * @throws SyntaxErrorException if a syntax error occurs during parsing
     * @throws DefectPipelineException if a pipeline stage cannot be instantiated
     */
    public static ICompiledProgram compileWithGlobal(String sourceCode) throws SyntaxErrorException, DefectPipelineException {
        return compile(sourceCode, globalInstructionSet);
    }

    /**
     * Compiles source code using the provided instruction set.
     *
     * @param sourceCode the source code to compile
     * @param set the instruction set to use
     * @return the compiled program
     * @throws SyntaxErrorException if a syntax error occurs during parsing
     * @throws DefectPipelineException if a pipeline stage cannot be instantiated
     */
    public static ICompiledProgram compile(String sourceCode, IInstructionSet set) throws SyntaxErrorException, DefectPipelineException {
        Objects.requireNonNull(set, "InstructionSet cannot be null");
        return tokenize(sourceCode, set).parse().compile();
    }

    /**
     * Tokenises source code using the global instruction set.
     *
     * @param sourceCode the source code to tokenise
     * @return a {@link TokenStage} representing the tokenisation stage
     * @throws DefectPipelineException if a pipeline stage cannot be instantiated
     */
    public static TokenStage tokenizeWithGlobal(String sourceCode) throws DefectPipelineException {
        return tokenize(sourceCode, globalInstructionSet);
    }

    /**
     * Tokenises source code using a specific instruction set.
     *
     * @param sourceCode the source code to tokenise
     * @param instructionSet the instruction set to use
     * @return a {@link TokenStage} representing the tokenisation stage
     * @throws DefectPipelineException if a pipeline stage cannot be instantiated
     */
    public static TokenStage tokenize(String sourceCode, IInstructionSet instructionSet) throws DefectPipelineException {
        Objects.requireNonNull(instructionSet, "InstructionSet cannot be null");
        ILexer lexer = newInstance(pipelineLexer);
        return new TokenStage(lexer.tokenize(sourceCode, instructionSet), instructionSet);
    }

    /**
     * Represents the tokenisation stage in the pipeline.
     *
     * <p>Allows chaining to the parse stage via {@link #parse()}.</p>
     */
    public record TokenStage(List<IToken> tokens, IInstructionSet set) {
        /**
         * Parses the tokens into a syntax tree.
         *
         * @return a {@link ParseStage} representing the parsing stage
         * @throws SyntaxErrorException if the source contains syntax errors
         * @throws DefectPipelineException if the parser cannot be instantiated
         */
        public ParseStage parse() throws SyntaxErrorException, DefectPipelineException {
            IParser parser = newInstance(pipelineParser);
            return new ParseStage(parser.parse(tokens), set);
        }
    }

    /**
     * Represents the parsing stage in the pipeline.
     *
     * <p>Allows chaining to the compile stage via {@link #compile()}.</p>
     */
    public record ParseStage(ISyntaxTree syntaxTree, IInstructionSet set) {
        /**
         * Compiles the syntax tree into a {@link ICompiledProgram}.
         *
         * @return the compiled program
         * @throws DefectPipelineException if the compiler cannot be instantiated
         */
        public ICompiledProgram compile() throws DefectPipelineException {
            ICompiler compiler = newInstance(pipelineCompiler);
            return compiler.compile(syntaxTree, set);
        }
    }

    /**
     * Instantiates a pipeline stage class using reflection.
     *
     * @param cls the class to instantiate
     * @param <T> the stage type
     * @return an instance of the pipeline stage
     * @throws DefectPipelineException if instantiation fails
     */
    private static <T> T newInstance(Class<? extends T> cls) throws DefectPipelineException {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DefectPipelineException("Failed to instantiate pipeline stage: " + cls.getName(), e);
        }
    }
}
