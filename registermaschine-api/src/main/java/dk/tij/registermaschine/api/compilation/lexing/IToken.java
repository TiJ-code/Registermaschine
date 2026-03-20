package dk.tij.registermaschine.api.compilation.lexing;

public interface IToken {
    TokenType type();
    int line();
    int column();
    String value();
}
