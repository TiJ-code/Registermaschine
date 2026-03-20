package dk.tij.registermaschine.api.compilation.lexing;

public enum TokenType {
    // instructions & operands
    INSTRUCTION,
    REGISTER,
    NUMBER,
    LABEL,
    LABEL_DEF,

    // structure
    COMMA,
    EOL,
    EOF,

    // comments
    COMMENT,

    // errors
    UNKNOWN,
    ERROR
}
