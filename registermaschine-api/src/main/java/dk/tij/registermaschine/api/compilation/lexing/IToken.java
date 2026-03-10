package dk.tij.registermaschine.api.compilation.lexing;

/**
 * Represents a lexical token produced by a {@link dk.tij.registermaschine.core.compilation.api.ILexer}.
 *
 * <p>A token contains the type, source location (line and column), and its raw text value.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IToken {
    /**
     * Returns the type of this token.
     *
     * @return the {@link TokenType}
     */
    TokenType type();

    /**
     * Returns the line number.
     *
     * @return the line number
     */
    int line();

    /**
     * Returns the column number.
     *
     * @return the column number.
     */
    int column();

    /**
     * Returns the raw text value.
     *
     * @return the value
     */
    String value();
}
