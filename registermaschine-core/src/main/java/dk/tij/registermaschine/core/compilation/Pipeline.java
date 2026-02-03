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

    public static ICompiledProgram compile(String sourceCode, InstructionSet set) throws SyntaxErrorException, DefectPipelineException {
        return tokenize(sourceCode).parse().compile(set);
    }

    public static TokenStage tokenize(String sourceCode) throws DefectPipelineException {
        ILexer lexer = newInstance(pipelineLexer);
        return new TokenStage(lexer.tokenize(sourceCode));
    }

    public record TokenStage(List<IToken> tokens) {
        public ParseStage parse() throws SyntaxErrorException, DefectPipelineException {
            IParser parser = newInstance(pipelineParser);
            return new ParseStage(parser.parse(tokens));
        }
    }

    public record ParseStage(ISyntaxTree syntaxTree) {
        public ICompiledProgram compile(InstructionSet set) throws DefectPipelineException {
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
