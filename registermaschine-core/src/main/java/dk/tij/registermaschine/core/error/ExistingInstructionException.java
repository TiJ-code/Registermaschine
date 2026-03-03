package dk.tij.registermaschine.core.error;

public class ExistingInstructionException extends RuntimeException {
    public ExistingInstructionException(String message) {
        super(message);
    }
}
