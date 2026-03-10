package dk.tij.registermaschine.core.config.api;

import org.w3c.dom.Element;

/**
 * Event object representing the successful parsing of a configuration element.
 *
 * <p>Contains the XML element that was parsed and the resulting
 * parsed object.</p>
 *
 * @param xmlElement the source XML element
 * @param result the object produced from parsing this element
 * @param <T> the type of the parsed result
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ParsingEvent<T>(Element xmlElement, T result) {}
