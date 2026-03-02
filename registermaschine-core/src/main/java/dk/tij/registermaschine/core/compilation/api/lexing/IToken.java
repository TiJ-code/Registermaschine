package dk.tij.registermaschine.core.compilation.api.lexing;

public interface IToken {
    TokenType type();
    int line();
    int column();
    String value();
}
