package dk.tij.registermaschine.api.error;

/**
 * Thrown when an instruction cannot be identified or is not registered
 * in the current instruction set.
 *
 * @since 1.0.0
 * @author TiJ
 */
public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException(String message) {
        super(message);
    }
}
