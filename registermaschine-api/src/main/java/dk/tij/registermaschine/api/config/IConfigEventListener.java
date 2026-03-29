package dk.tij.registermaschine.api.config;

/**
 * Listener interface for receiving notifications when configuration elements
 * are successfully parsed.
 *
 * <p>Implementers can register with a parser to react to parsing events
 * and perform additional validation, logging, or side effects.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface IConfigEventListener {
    /**
     * Called when a configuration element has been parsed.
     *
     * @param event the parsing event containing the XML element
     *              and parsed result
     */
    void onElementParsed(ParsingEvent<?> event);
}
