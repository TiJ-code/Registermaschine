package dk.tij.registermaschine.core.error;

public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException(String message) {
        super(message);
    }
}
