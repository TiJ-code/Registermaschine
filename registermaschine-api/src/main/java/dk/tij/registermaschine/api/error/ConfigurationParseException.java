package dk.tij.registermaschine.api.error;

public class ConfigurationParseException extends RuntimeException {
    public ConfigurationParseException(String message, Exception e) {
        super(message, e);
    }

    public ConfigurationParseException(String message) {
        super(message);
    }
}
