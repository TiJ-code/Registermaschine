package dk.tij.registermaschine.api.config;

import org.w3c.dom.Element;

/**
 * Represents the result of parsing a configuration element.
 *
 * <p>Contains the source element and the corresponding parsed object.</p>
 *
 * <p>This interface does not define how parsing is performed.</p>
 *
 * @param xmlElement the source element
 * @param result the parsed result
 * @param <T> the result type
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ParsingEvent<T>(Element xmlElement, T result) {}
