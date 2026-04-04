package dk.tij.registermaschine.api.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Defines the contract for a configuration parser.
 *
 * <p>A configuration parser processes a structured input and produces
 * corresponding configuration objects. Implementations may emit
 * {@link ParsingEvent events} during parsing.</p>
 *
 * <p>This interface does not define how listeners are stored or managed.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IConfigParser {
    /**
     * Thread-safe list of listeners to notify when elements are parsed
     */
    List<IConfigEventListener> LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Parses the given configuration document.
     *
     * @param xmlDocument the configuration input
     */
    void parseConfig(Document xmlDocument);

    /**
     * Registers a listener for parsing events.
     *
     * @param listener the listener to add
     */
    default void addListener(IConfigEventListener listener) {
        LISTENERS.add(listener);
    }

    /**
     * Fires a {@link ParsingEvent} to all registered listeners.
     *
     * @param xmlElement the XML element that was parsed
     * @param result the result of parsing the element
     */
    default void fireEvent(Element xmlElement, Object result) {
        ParsingEvent<?> event = new ParsingEvent<>(xmlElement, result);
        LISTENERS.forEach(listener -> listener.onElementParsed(event));
    }
}
