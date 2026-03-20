package dk.tij.registermaschine.api.config;

@FunctionalInterface
public interface IConfigEventListener {
    void onElementParsed(ParsingEvent<?> event);
}
