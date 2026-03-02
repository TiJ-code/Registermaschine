package dk.tij.registermaschine.core.config.api;

import org.w3c.dom.Element;

public record ParsingEvent<T>(Element xmlElement, T result) {}
