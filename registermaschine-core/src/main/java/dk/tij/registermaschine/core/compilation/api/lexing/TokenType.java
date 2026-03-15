package dk.tij.registermaschine.core.compilation.api.lexing;

public enum TokenType {
    // instructions & operands
    INSTRUCTION,
    REGISTER,
    NUMBER,
    ADDRESS,
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
