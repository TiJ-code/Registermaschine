package dk.tij.registermaschine.core.compilation.internal.lexing;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.lexing.TokenType;

public record ConcreteToken(TokenType type, String value, int line, int column) implements IToken {
    @Override
    public String toString() {
        return String.format("%s ('%s')", type, value);
    }
}
