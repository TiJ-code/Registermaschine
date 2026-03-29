package dk.tij.registermaschine.api.error;

/**
 * Thrown when a class cannot be instantiated via reflection or other
 * dynamic mechanisms.
 *
 * <p>This exception wraps the underlying exception that caused the failure.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ClassInstantiationException extends RuntimeException {
    public ClassInstantiationException(String message, Exception e) {
        super(message, e);
    }
}
