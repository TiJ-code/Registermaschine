package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.ILexer;
import dk.tij.registermaschine.core.compilation.api.IParser;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.core.error.DefectPipelineException;
import dk.tij.registermaschine.core.error.SyntaxErrorException;

import java.util.List;
import java.util.Objects;

public final class Pipeline {
    private Pipeline() {}

    private static Class<? extends ILexer> pipelineLexer = ConcreteLexer.class;
    private static Class<? extends IParser> pipelineParser = ConcreteParser.class;
    private static Class<? extends ICompiler> pipelineCompiler = ConcreteCompiler.class;
    private static InstructionSet globalInstructionSet;

    public static void useLexer(Class<? extends ILexer> customLexer) {
        Objects.requireNonNull(customLexer, "Custom Lexer cannot be null");
        pipelineLexer = customLexer;
    }

    public static void useParser(Class<? extends IParser> customParser) {
        Objects.requireNonNull(customParser, "Custom Parser cannot be null");
        pipelineParser = customParser;
    }

    public static void useCompiler(Class<? extends ICompiler> customCompiler) {
        Objects.requireNonNull(customCompiler, "Custom Compiler cannot be null");
        pipelineCompiler = customCompiler;
    }

    public static void setGlobalInstructionSet(InstructionSet set) {
        Objects.requireNonNull(set, "Custom global InstructionSet cannot be null");
        globalInstructionSet = set;
    }

    /**
     * Compiles the given source code with a configured global {@link InstructionSet}
     * @param sourceCode The source code to compile
     * @return The compiled program
     * @throws SyntaxErrorException Thrown when there is a syntax error within the source code
     * @throws DefectPipelineException Thrown when the pipeline is defected
     */
    public static ICompiledProgram compileWithGlobal(String sourceCode) throws SyntaxErrorException, DefectPipelineException {
        return compile(sourceCode, globalInstructionSet);
    }

    public static ICompiledProgram compile(String sourceCode, InstructionSet set) throws SyntaxErrorException, DefectPipelineException {
        Objects.requireNonNull(set, "InstructionSet cannot be null");
        return tokenize(sourceCode, set).parse().compile();
    }

    /**
     * Tokenises the given source code with a configured global {@link InstructionSet}
     * @param sourceCode The source code to tokenise
     * @return A pipeline stage
     * @throws SyntaxErrorException Thrown when there is a syntax error within the source code
     * @throws DefectPipelineException Thrown when the pipeline is defected
     */
    public static TokenStage tokenizeWithGlobal(String sourceCode) throws DefectPipelineException {
        return tokenize(sourceCode, globalInstructionSet);
    }

    public static TokenStage tokenize(String sourceCode, InstructionSet instructionSet) throws DefectPipelineException {
        Objects.requireNonNull(instructionSet, "InstructionSet cannot be null");
        ILexer lexer = newInstance(pipelineLexer);
        return new TokenStage(lexer.tokenize(sourceCode, instructionSet), instructionSet);
    }

    public record TokenStage(List<IToken> tokens, InstructionSet set) {
        public ParseStage parse() throws SyntaxErrorException, DefectPipelineException {
            IParser parser = newInstance(pipelineParser);
            return new ParseStage(parser.parse(tokens), set);
        }
    }

    public record ParseStage(ISyntaxTree syntaxTree, InstructionSet set) {
        public ICompiledProgram compile() throws DefectPipelineException {
            ICompiler compiler = newInstance(pipelineCompiler);
            return compiler.compile(syntaxTree, set);
        }
    }

    private static <T> T newInstance(Class<? extends T> cls) throws DefectPipelineException {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DefectPipelineException("Failed to instantiate pipeline stage: " + cls.getName(), e);
        }
    }
}
