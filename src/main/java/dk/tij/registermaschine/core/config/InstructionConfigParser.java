package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.conditions.Condition;
import dk.tij.registermaschine.core.conditions.NotCondition;
import dk.tij.registermaschine.core.conditions.OrCondition;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.JumpInstruction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class InstructionConfigParser {
    private final InstructionRegistry registry;

    public InstructionConfigParser(InstructionRegistry registry) {
        this.registry = registry;
    }

    public void parseConfig(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        NodeList instructionNodes = doc.getElementsByTagName("instruction");

        for (int i = 0; i < instructionNodes.getLength(); i++) {
            Element element = (Element) instructionNodes.item(i);

            String id = element.getAttribute("id");
            byte opcode = Byte.decode(element.getAttribute("opcode"));
            int operands = Integer.parseInt(element.getAttribute("operands"));
            String conditionStr = element.getAttribute("condition");
            String handlerStr = element.getAttribute("handler");

            Condition condition = parseCondition(conditionStr);

            if (id.startsWith("j")) {
                JumpInstruction jump = new JumpInstruction(opcode, condition);
                registry.registerInstruction(opcode, jump);
            } else {
                Class<? extends AbstractInstruction> cls = parseHandler(handlerStr);
                AbstractInstruction instruction = cls.getDeclaredConstructor(byte.class, int.class, Condition.class)
                                .newInstance(opcode, operands, condition);
                registry.registerInstruction(opcode, instruction);
            }
        }
    }

    private Condition parseCondition(String condStr) throws Exception {
        if (condStr == null || condStr.isEmpty()) return null;

        String[] parts = condStr.split(",");
        Condition combined = null;

        for (String part : parts) {
            part = part.trim();
            boolean negate = part.startsWith("!");
            String className = negate ? part.substring(1) : part;

            Class<?> cls = Class.forName("dk.tij.registermaschine." + className);
            Condition cond = (Condition) cls.getDeclaredConstructor().newInstance();

            if (negate) cond = new NotCondition(cond);

            if (combined == null) combined = cond;
            else combined = new OrCondition(combined, cond);
        }

        return combined;
    }

    private Class<? extends AbstractInstruction> parseHandler(String handlerStr) throws Exception {
        if (handlerStr == null || handlerStr.isEmpty()) return null;

        return Class.forName("dk.tij.registermaschine." + handlerStr.trim())
                .asSubclass(AbstractInstruction.class);
    }

    private AbstractInstruction createInstruction(String className, int opcode, int operands, Condition condition) throws Exception {
        Class<?> cls = Class.forName(className);

        return (AbstractInstruction) cls
                .getDeclaredConstructor(int.class, Condition.class)
                .newInstance(opcode, condition);
    }
}
