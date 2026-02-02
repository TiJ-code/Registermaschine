package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.*;
import dk.tij.registermaschine.core.compilation.internal.Compiler;
import dk.tij.registermaschine.core.compilation.internal.Lexer;
import dk.tij.registermaschine.core.compilation.internal.Parser;
import dk.tij.registermaschine.core.config.InstructionSet;

public final class Pipeline {
    private Pipeline() {}

    public static TokenStage tokenize(String sourceCode) {
        return new TokenStage(Lexer.tokenize(sourceCode));
    }

    public record TokenStage(TokenCollection tokens) {
        public ParseStage parse() {
            return new ParseStage(Parser.parse(tokens));
        }
    }

    public record ParseStage(AbstractSyntaxTree tokens) {
        public CompiledProgram compile(InstructionSet set) {
            return Compiler.compile(tokens, set);
        }
    }
}
