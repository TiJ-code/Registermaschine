package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.log.Logger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses macro definitions for logical conditions from the configuration XML.
 *
 * <p>The parser looks for {@code <conditionMacro>} tags and populates the global
 * {@link CoreConfig#CONDITION_MACROS} map. These macros can later be referenced
 * in the {@link dk.tij.registermaschine.core.config.internal.conditions.ConditionBuilder}
 * using the {@code @} symbol.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConditionMacroParser implements IConfigParser {
    private static final Logger log = LoggerFactory.getLogger(ConditionMacroParser.class);

    /**
     * Extracts name-value pairs from macro elements in the XML document.
     *
     * @param xmlDocument the parsed DOM element of the configuration file.
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        log.info("Started parsing condition macros");

        NodeList macroNodes = xmlDocument.getElementsByTagName("conditionMacro");

        for (int i = 0; i < macroNodes.getLength(); i++) {
            Element macroElem = (Element) macroNodes.item(i);

            String name = macroElem.getAttribute(XmlConstants.ATTRIBUTE_CONDITION_MACRO_NAME);
            String value = macroElem.getAttribute(XmlConstants.ATTRIBUTE_CONDITION_MACRO_VALUE);

            if (!name.isEmpty() && !value.isEmpty()) {
                log.info("Parsing macro @{} with value {}", name, value);
                CoreConfig.CONDITION_MACROS.put(name, value);
            }
        }
    }
}
