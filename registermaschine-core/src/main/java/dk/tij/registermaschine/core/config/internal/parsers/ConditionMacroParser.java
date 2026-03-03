package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class ConditionMacroParser implements IConfigParser {
    @Override
    public void parseConfig(Document xmlDocument) {
        NodeList macroNodes = xmlDocument.getElementsByTagName("conditionMacro");

        for (int i = 0; i < macroNodes.getLength(); i++) {
            Element macroElem = (Element) macroNodes.item(i);

            String name = macroElem.getAttribute(XmlConstants.ATTRIBUTE_CONDITION_MACRO_NAME);
            String value = macroElem.getAttribute(XmlConstants.ATTRIBUTE_CONDITION_MACRO_VALUE);

            if (!name.isEmpty() && !value.isEmpty())
                CoreConfig.CONDITION_MACROS.put(name, value);
        }
    }
}
