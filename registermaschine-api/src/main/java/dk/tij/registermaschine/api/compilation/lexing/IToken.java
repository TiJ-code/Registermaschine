package dk.tij.registermaschine.api.compilation.lexing;

/**
 * Represents a lexical token produced during the lexing phase.
 *
 * <p>A token encapsulates a {@link TokenType type}, its position in the
 * source input, and its textual representation.</p>
 *
 * <p>This interface defines the contract for tokens consumed by later
 * compilation stages (e.g. parsing), but does not specify how tokens
 * are created.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IToken {
    /**
     * Returns the type of this token.
     *
     * @return the token type
     */
    TokenType type();

    /**
     * Returns the line number in the source input.
     *
     * <p>Line numbering is implementation-defined but is typically 1-based.</p>
     *
     * @return the line number
     */
    int line();

    /**
     * Returns the column number in the source input.
     *
     * <p>Column numbering is implementation-defined but is typically 1-based.</p>
     *
     * @return the column number
     */
    int column();

    /**
     * Returns the raw textual representation of this token.
     *
     * <p>No interpretation or normalisation is implied.</p>
     *
     * @return the token text
     */
    String value();
}
