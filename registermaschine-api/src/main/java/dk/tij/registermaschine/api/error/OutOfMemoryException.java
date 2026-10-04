package dk.tij.registermaschine.api.error;

/**
 * Thrown when a memory allocation or configuration exceeds the available system memory limits.
 *
 * <p>This exception is used during configuration or runtime setup when a requested memory size
 * cannot be satisfied by the host system or violates defined memory constraints.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public class OutOfMemoryException extends RuntimeException {
    public OutOfMemoryException(String message) {
        super(message);
    }
}
