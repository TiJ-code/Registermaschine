package dk.tij.registermaschine.api.error;

/**
 * Thrown when attempting to register an instruction that already exists
 * in the instruction set.
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ExistingInstructionException extends RuntimeException {
    public ExistingInstructionException(String message) {
        super(message);
    }
}
