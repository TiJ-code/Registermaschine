package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class SettingsParser implements IConfigParser {
    @Override
    public void parseConfig(Document xmlDocument) {
        NodeList registerNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_REGISTERS);
        if (registerNodeList.getLength() > 0) {
            CoreConfig.REGISTERS = Math.max(Integer.parseInt(registerNodeList.item(0).getTextContent()), 1);
        }

        NodeList maxJumpsNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_MAX_JUMPS);
        if (maxJumpsNodeList.getLength() > 0) {
            CoreConfig.MAX_JUMPS = Math.max(Integer.parseInt(maxJumpsNodeList.item(0).getTextContent()), 1);
        }
    }
}
