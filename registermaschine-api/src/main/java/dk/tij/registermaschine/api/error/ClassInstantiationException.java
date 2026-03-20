package dk.tij.registermaschine.api.error;

public class ClassInstantiationException extends RuntimeException {
    public ClassInstantiationException(String message, Exception e) {
        super(message, e);
    }
}
