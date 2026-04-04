package dk.tij.registermaschine.core.compilation.internal.lexing;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.lexing.TokenType;

/**
 * A concrete implementation of a lexical token.
 *
 * <p>This record server as a data carries for the results of the lexing phase,
 * storing the classification of the text, its literal value, and its precise
 * location within the source code for error reporting.</p>
 *
 * @param type   The category of the token (e.g. {@link TokenType#REGISTER}, {@link TokenType#LABEL})
 * @param value  The raw string literal as it appeared in the source code
 * @param line   The 1-based line number where this token starts
 * @param column The 1-based column position where this token starts.
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConcreteToken(TokenType type, String value, int line, int column) implements IToken {
    @Override
    public String toString() {
        return String.format("%s ('%s')", type, value);
    }
}
