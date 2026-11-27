package tij.registermaschine.api.compilation.lexing;

/**
 * Creates tokens for a lexical analysis stage.
 *
 * @since 2.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface ITokenFactory {
    /**
     * Creates a token with the given type, text and source position.
     *
     * @param type the token type
     * @param value the token text, which may be null for tokens such as EOF
     * @param line the line number in the source input
     * @param column the column number in the source input
     * @return the created token
     */
    IToken create(TokenType type, String value, int line, int column);
}
