package dk.tij.registermaschine.core.error;

public class SyntaxErrorException extends RuntimeException {
    public SyntaxErrorException(String message) {
        super(message);
    }
}
