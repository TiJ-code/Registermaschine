package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class InstructionSetOptionParser implements IConfigParser {
    @Override
    public void parseConfig(Document xmlDocument) {
        NodeList optionsList = xmlDocument.getElementsByTagName(XmlConstants.TAG_OPTION);
        for (int i = 0; i < optionsList.getLength(); i++) {
            Element option = (Element) optionsList.item(i);
            if (XmlConstants.INSTR_OPTION_ALLOW_LABELS.equals(option.getAttribute(XmlConstants.ATTRIBUTE_OPTION_ID))) {
                CoreConfig.ALLOW_LABELS = Boolean.parseBoolean(option.getAttribute(XmlConstants.ATTRIBUTE_OPTION_VALUE));
                fireEvent(option, CoreConfig.ALLOW_LABELS);
            }
        }
    }
}
