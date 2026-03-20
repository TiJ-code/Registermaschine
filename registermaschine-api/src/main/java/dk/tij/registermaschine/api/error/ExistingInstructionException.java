package dk.tij.registermaschine.api.error;

public class ExistingInstructionException extends RuntimeException {
    public ExistingInstructionException(String message) {
        super(message);
    }
}
