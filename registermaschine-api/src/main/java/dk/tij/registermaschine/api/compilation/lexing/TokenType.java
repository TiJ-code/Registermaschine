package dk.tij.registermaschine.api.compilation.lexing;

/**
 * Enumerates the possible types of lexical tokens in the language.
 *
 * <p>Tokens can represent instructions, operands, structural symbols,
 * comments, or error types.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
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
