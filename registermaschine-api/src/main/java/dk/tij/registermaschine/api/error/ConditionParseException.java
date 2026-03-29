package dk.tij.registermaschine.api.error;

/**
 * Thrown when a condition expression cannot be parsed or is invalid.
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConditionParseException extends RuntimeException {
    public ConditionParseException(String message) {
        super(message);
    }
}
