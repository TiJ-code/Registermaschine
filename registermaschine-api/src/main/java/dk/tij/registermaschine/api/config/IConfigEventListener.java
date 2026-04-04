package dk.tij.registermaschine.api.config;

/**
 * Listener for receiving configuration parsing events.
 *
 * <p>Implementations may use these events for validation, logging,
 * or additional processing.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface IConfigEventListener {
    /**
     * Called when a configuration element has been parsed.
     *
     * @param event the parsing event
     */
    void onElementParsed(ParsingEvent<?> event);
}
