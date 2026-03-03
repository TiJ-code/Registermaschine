package dk.tij.registermaschine.core.config.api;

@FunctionalInterface
public interface IConfigEventListener {
    void onElementParsed(ParsingEvent<?> event);
}
