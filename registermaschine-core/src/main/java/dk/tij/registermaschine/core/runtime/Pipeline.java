package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.ILexer;
import dk.tij.registermaschine.core.compilation.api.IParser;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.internal.Compiler;
import dk.tij.registermaschine.core.compilation.internal.Lexer;
import dk.tij.registermaschine.core.compilation.internal.Parser;
import dk.tij.registermaschine.core.config.InstructionSet;

import java.util.List;

public final class Pipeline {
    private Pipeline() {}

    private static Class<? extends ILexer> pipelineLexer = Lexer.class;
    private static Class<? extends IParser> pipelineParser = Parser.class;
    private static Class<? extends ICompiler> pipelineCompiler = Compiler.class;

    public static void useLexer(Class<? extends ILexer> customLexer) {
        pipelineLexer = customLexer;
    }

    public static void useParser(Class<? extends IParser> customParser) {
        pipelineParser = customParser;
    }

    public static void useCompiler(Class<? extends ICompiler> customCompiler) {
        pipelineCompiler = customCompiler;
    }

    public static TokenStage tokenize(String sourceCode) {
        ILexer lexer = newInstance(pipelineLexer);
        return new TokenStage(lexer.tokenize(sourceCode));
    }

    public record TokenStage(List<IToken> tokens) {
        public ParseStage parse() {
            IParser parser = newInstance(pipelineParser);
            return new ParseStage(parser.parse(tokens));
        }
    }

    public record ParseStage(ISyntaxTree syntaxTree) {
        public ICompiledProgram compile(InstructionSet set) {
            ICompiler compiler = newInstance(pipelineCompiler);
            return compiler.compile(syntaxTree, set);
        }
    }

    private static <T> T newInstance(Class<? extends T> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate pipeline stage: " + cls.getName(), e);
        }
    }
}
