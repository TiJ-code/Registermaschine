package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.conditions.Condition;
import dk.tij.registermaschine.core.conditions.NotCondition;
import dk.tij.registermaschine.core.conditions.OrCondition;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.JumpInstruction;
import dk.tij.registermaschine.core.parser.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class InstructionConfigParser {
    private static final String CLASS_PATH_PREFIX = "dk.tij.registermaschine.";

    private final InstructionRegistry registry;
    private final IConfigParser customConfigParser;

    public InstructionConfigParser(InstructionRegistry registry) {
        this.registry = registry;
        this.customConfigParser = null;
    }

    public InstructionConfigParser(InstructionRegistry registry, IConfigParser customConfigParser) {
        this.registry = registry;
        this.customConfigParser = customConfigParser;
    }

    public void parseConfig(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        parseRegisters(doc);
        parseTokenColours(doc);
        parseInstructions(doc);

        if (customConfigParser != null)
            customConfigParser.parseConfig(doc);
    }

    private void parseRegisters(Document document) throws Exception {
        NodeList registerNodeList = document.getElementsByTagName("registers");

        if (registerNodeList.getLength() < 1) return;

        Config.REGISTERS = Math.max(Integer.parseInt(registerNodeList.item(0).getTextContent()), 1);

        System.out.println("Parsed Registers: " + Config.REGISTERS);
    }

    private void parseTokenColours(Document document) throws Exception {
        NodeList colourNodes = document.getElementsByTagName("colours");

        for (int i = 0; i < colourNodes.getLength(); i++) {
            Element element = (Element) colourNodes.item(i);


            String tokenId = element.getAttribute("tokenId");
            String hexString = element.getTextContent();

            Token.Type type = null;
            try {
                type = Token.Type.valueOf(tokenId);
            } catch (IllegalArgumentException ignored) {}

            if (type != null)
                Config.TOKEN_COLOUR.put(type, hexString);
        }
        System.out.println(Config.TOKEN_COLOUR);
    }

    private void parseInstructions(Document document) throws Exception {
        NodeList instructionNodes = document.getElementsByTagName("instruction");

        for (int i = 0; i < instructionNodes.getLength(); i++) {
            Element element = (Element) instructionNodes.item(i);

            String id = element.getAttribute("id");
            byte opcode = Byte.decode(element.getAttribute("opcode"));
            int operands = Integer.parseInt(element.getAttribute("operands"));
            String conditionStr = element.getAttribute("condition");
            String handlerStr = element.getAttribute("handler");
            String description = element.getAttribute("description");

            InstructionDescriptor descriptor = new InstructionDescriptor(id.toUpperCase(), description);

            Condition condition = parseCondition(conditionStr);

            if (id.startsWith("j")) {
                JumpInstruction jump = new JumpInstruction(opcode, condition);
                registry.registerInstruction(id, opcode, descriptor, jump);
            } else {
                registry.registerInstruction(id, opcode, descriptor,
                        createInstruction(parseHandler(handlerStr),
                                opcode, operands, condition));
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

            Class<?> cls = Class.forName(CLASS_PATH_PREFIX + className);
            Condition cond = (Condition) cls.getDeclaredConstructor().newInstance();

            if (negate) cond = new NotCondition(cond);

            if (combined == null) combined = cond;
            else combined = new OrCondition(combined, cond);
        }

        return combined;
    }

    private Class<? extends AbstractInstruction> parseHandler(String handlerStr) throws Exception {
        if (handlerStr == null || handlerStr.isEmpty()) return null;

        return Class.forName(CLASS_PATH_PREFIX + handlerStr.trim())
                .asSubclass(AbstractInstruction.class);
    }

    private AbstractInstruction createInstruction(Class<? extends AbstractInstruction> clazz, byte opcode, int operands, Condition condition) throws Exception {
        return clazz
                .getDeclaredConstructor(byte.class, int.class, Condition.class)
                .newInstance(opcode, operands, condition);
    }
}
