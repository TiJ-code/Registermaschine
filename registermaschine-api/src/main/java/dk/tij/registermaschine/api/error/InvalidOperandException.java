package dk.tij.registermaschine.api.error;

public class InvalidOperandException extends RuntimeException {
    public InvalidOperandException(String message) {
        super(message);
    }
}
