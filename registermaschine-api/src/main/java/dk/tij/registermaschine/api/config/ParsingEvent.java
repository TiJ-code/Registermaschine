package dk.tij.registermaschine.api.config;

import org.w3c.dom.Element;

public record ParsingEvent<T>(Element xmlElement, T result) {}
