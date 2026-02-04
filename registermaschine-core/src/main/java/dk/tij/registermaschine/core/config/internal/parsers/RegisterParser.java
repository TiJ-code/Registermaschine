package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class RegisterParser implements IConfigParser {
    @Override
    public void parseConfig(Document xmlDocument) {
        NodeList registerNodeList = xmlDocument.getElementsByTagName("registers");

        if (registerNodeList.getLength() < 1) return;

        CoreConfig.REGISTERS = Math.max(Integer.parseInt(registerNodeList.item(0).getTextContent()), 1);
    }
}
