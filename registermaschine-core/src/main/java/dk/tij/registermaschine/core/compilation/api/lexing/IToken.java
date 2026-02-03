package dk.tij.registermaschine.core.compilation.api.lexing;

public interface IToken {
    ITokenType type();
    int line();
    int column();
    String value();
}
