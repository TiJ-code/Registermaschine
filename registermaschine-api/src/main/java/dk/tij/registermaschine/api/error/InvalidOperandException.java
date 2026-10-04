package dk.tij.registermaschine.api.error;

/**
 * Thrown when one or more operands are invalid for a given instruction or step.
 *
 * @since 2.0.0
 * @author TiJ
 */
public class InvalidOperandException extends RuntimeException {
    public InvalidOperandException(String message) {
        super(message);
    }
}
