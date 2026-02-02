package dk.tij.registermaschine.core.exception;

public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException(String message) {
        super(message);
    }
}
