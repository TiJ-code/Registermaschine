package dk.tij.registermaschine.core.compilation.lexing;

import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.lexing.ITokenType;

public record Token(ITokenType type, String value, int line, int column) implements IToken {
    public enum Type implements ITokenType {
        INSTRUCTION,
        REGISTER,
        NUMBER,
        COMMENT,
        EOL,
        EOF,
        UNKNOWN
    }

    @Override
    public String toString() {
        return String.format("%s ('%s')", type, value);
    }
}
