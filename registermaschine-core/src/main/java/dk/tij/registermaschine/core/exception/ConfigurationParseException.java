package dk.tij.registermaschine.core.exception;

public class ConfigurationParseException extends RuntimeException {
    public ConfigurationParseException(Exception e) {
        super(e);
    }

    public ConfigurationParseException(String message) {
        super(message);
    }
}
