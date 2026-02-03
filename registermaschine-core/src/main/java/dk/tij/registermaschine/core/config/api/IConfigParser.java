package dk.tij.registermaschine.core.config.api;

import org.w3c.dom.Document;

public interface IConfigParser {
    void parseConfig(Document xmlDocument);
}
