package dk.tij.registermaschine.api.error;

/**
 * Thrown when there is a syntax error in the source code.
 *
 * @since 1.0.0
 * @author TiJ
 */
public class SyntaxErrorException extends RuntimeException {
    public SyntaxErrorException(String message) {
        super(message);
    }
}
