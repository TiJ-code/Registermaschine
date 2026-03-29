package dk.tij.registermaschine.api.error;

/**
 * Thrown when a configuration file or object cannot be parsed correctly.
 *
 * <p>This exception may wrap another exception that triggered the parse failure.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConfigurationParseException extends RuntimeException {
    public ConfigurationParseException(String message, Exception e) {
        super(message, e);
    }

    public ConfigurationParseException(String message) {
        super(message);
    }
}
