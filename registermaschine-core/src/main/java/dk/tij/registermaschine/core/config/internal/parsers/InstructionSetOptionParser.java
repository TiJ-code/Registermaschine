package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses global instruction set options from the XML configuration.
 *
 * <p>This parser processes {@code <option>} elements defined in the instruction
 * set file and applies their values to {@link CoreConfig}.</p>
 *
 * <p>Currently supported options include:</p>
 * <ul>
 *     <li>{@code allowLabels}: enables or disables label support in instructions</li>
 * </ul>
 *
 * <p>Each parsed option triggers a configuration event via
 * {@link #fireEvent(Element, Object)} for external listeners.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class InstructionSetOptionParser implements IConfigParser {
    private static final ILogger log = LoggerFactory.getLogger(InstructionSetOptionParser.class);

    /**
     * Parses all {@code <option>} elements from the given XML document and applies
     * their values to the core configuration.
     *
     * @param xmlDocument the XML document containing instruction set configuration
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        log.info("Started parsing options for this instruction set");

        NodeList optionsList = xmlDocument.getElementsByTagName(XmlConstants.TAG_OPTION);
        for (int i = 0; i < optionsList.getLength(); i++) {
            log.debug("Parsing <{}> tag {}", XmlConstants.TAG_OPTION, i);

            Element option = (Element) optionsList.item(i);
            if (XmlConstants.INSTR_OPTION_ALLOW_LABELS.equals(option.getAttribute(XmlConstants.ATTRIBUTE_OPTION_ID))) {
                CoreConfig.ALLOW_LABELS = Boolean.parseBoolean(option.getAttribute(XmlConstants.ATTRIBUTE_OPTION_VALUE));
                log.info("Parsed allow labels; this instruction set {}allows labels", CoreConfig.ALLOW_LABELS ? "" : "dis");
                fireEvent(option, CoreConfig.ALLOW_LABELS);
            }
        }
    }
}
