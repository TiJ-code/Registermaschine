package dk.tij.registermaschine.api.compilation.lexing;

/**
 * Enumerates the types of lexical tokens recognised by the language.
 *
 * <p>Token types define the syntactic role of a token and are used
 * by the parser to interpret the input stream.</p>
 *
 * <p>The exact set of supported tokens may vary depending on the
 * language or instruction set.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public enum TokenType {
    /**
     * Instruction mnemonic.
     */
    INSTRUCTION,

    /**
     * Register identifier.
     */
    REGISTER,

    /**
     * Numeric literal.
     */
    NUMBER,

    /**
     * Raw address reference
     */
    ADDRESS,

    /**
     * Label reference.
     */
    LABEL,

    /**
     * Label definition.
     */
    LABEL_DEF,

    /**
     * Separator between operands.
     */
    COMMA,

    /**
     * End of line.
     */
    EOL,

    /**
     * End of input.
     */
    EOF,

    /**
     * Comment token.
     */
    COMMENT,

    /**
     * Unrecognised token.
     */
    UNKNOWN,

    /**
     * Token representing a lexing error.
     */
    ERROR
}
