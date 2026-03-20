package dk.tij.registermaschine.api.error;

public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException(String message) {
        super(message);
    }
}
