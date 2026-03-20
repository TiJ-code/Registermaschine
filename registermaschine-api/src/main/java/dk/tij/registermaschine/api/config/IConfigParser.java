package dk.tij.registermaschine.api.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface IConfigParser {
    List<IConfigEventListener> LISTENERS = new CopyOnWriteArrayList<>();

    void parseConfig(Document xmlDocument);

    default void addListener(IConfigEventListener listener) {
        LISTENERS.add(listener);
    }

    default void fireEvent(Element xmlElement, Object result) {
        ParsingEvent<?> event = new ParsingEvent<>(xmlElement, result);
        LISTENERS.forEach(listener -> listener.onElementParsed(event));
    }
}
