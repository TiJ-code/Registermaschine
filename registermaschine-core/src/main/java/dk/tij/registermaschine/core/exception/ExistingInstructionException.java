package dk.tij.registermaschine.core.exception;

public class ExistingInstructionException extends RuntimeException {
    public ExistingInstructionException(String message) {
        super(message);
    }
}
