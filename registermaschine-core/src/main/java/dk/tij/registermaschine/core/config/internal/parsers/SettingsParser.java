package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses general machine constraints and settings from the configuration XML.
 *
 * <p>This parses handles fundamental register machine parameters such as the number of
 * available registers and the maximum allowed jump limit (to prevent infinite loops during
 * execution).</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class SettingsParser implements IConfigParser {
    private static final Logger log = LoggerFactory.getLogger(SettingsParser.class);

    /**
     * Parses numeric settings and updates the global {@link CoreConfig} state.
     *
     * <p>Both registers and max jumps are clamped to a minimum value of 1 to
     * ensure machine stability.</p>
     *
     * @param xmlDocument the parsed DOM document of the configuration file
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        log.info("Started parsing settings");

        NodeList registerNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_REGISTERS);
        if (registerNodeList.getLength() > 0) {
            CoreConfig.REGISTERS = Math.max(Integer.parseInt(registerNodeList.item(0).getTextContent()), 1);
            log.info("Parsed registers for configuration; interpreting {}", CoreConfig.REGISTERS);
        } else
            log.warn("No <{}> element found in config", XmlConstants.TAG_CONFIG_REGISTERS);

        NodeList maxJumpsNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_MAX_JUMPS);
        if (maxJumpsNodeList.getLength() > 0) {
            CoreConfig.MAX_JUMPS = Math.max(Integer.parseInt(maxJumpsNodeList.item(0).getTextContent()), 1);
            log.info("Parsed max jumps for configuration; interpreting {}", CoreConfig.MAX_JUMPS);
        } else
            log.warn("No <{}> element found in config", XmlConstants.TAG_CONFIG_MAX_JUMPS);
        }

        NodeList customNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_CUSTOM);
        for (int i = 0; i < customNodeList.getLength(); i++) {
            Element customElement = (Element) customNodeList.item(i);
            fireEvent(customElement, customElement.getTextContent());
        }
    }
}
