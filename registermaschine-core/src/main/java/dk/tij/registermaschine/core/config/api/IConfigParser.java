package dk.tij.registermaschine.core.config.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Parser interface for processing XML-based configuration documents.
 *
 * <p>Provides methods to parse a {@link Document} and notify registered
 * {@link IConfigEventListener listeners} about each parsed element.</p>
 *
 * <p>Implementations can extend this interface and override
 * {@link #parseConfig(Document)} to provide the specific parsing logic.</p>
 *
 * <p>The listener list is thread-safe, using {@link CopyOnWriteArrayList}</p>
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
     * Parses a given XML document and fires events for each element
     * that is successfully parsed.
     *
     * @param xmlDocument the XML configuration document to parse
     */
    void parseConfig(Document xmlDocument);

    /**
     * Registers a new listener to receive parsing events.
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
