package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.conditions.internal.AndCondition;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.conditions.internal.NotCondition;
import dk.tij.registermaschine.core.conditions.internal.OrCondition;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.conditionParser.*;
import dk.tij.registermaschine.core.config.conditionParser.nodes.*;
import dk.tij.registermaschine.core.exception.ConfigurationParseException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.JumpInstruction;
import dk.tij.registermaschine.core.compilation.lexing.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

public final class CoreConfigParser {
    private CoreConfigParser() {}

    private static final String CORE_IMPLEMENTATION = "core.",
                                CLASS_PATH_PREFIX = "dk.tij.registermaschine.";

    public static void parseCoreConfig(InstructionSet set) throws ConfigurationParseException {
        parseConfig(set, null);
    }

    public static void parseConfig(InstructionSet set, IConfigParser customConfigParser) throws ConfigurationParseException {
        try (InputStream is = CoreConfigParser.class.getClassLoader().getResourceAsStream("configuration.jxml")) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            parseRegisters(doc);
            parseTokenColours(doc);
            parseInstructions(doc, set);

            if (customConfigParser != null)
                customConfigParser.parseConfig(doc);
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }

    private static void parseRegisters(Document document) {
        NodeList registerNodeList = document.getElementsByTagName("registers");

        if (registerNodeList.getLength() < 1) return;

        Config.REGISTERS = Math.max(Integer.parseInt(registerNodeList.item(0).getTextContent()), 1);
    }

    private static void parseTokenColours(Document document) {
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
    }

    private static void parseInstructions(Document document, InstructionSet set) throws Exception {
        NodeList instructionNodes = document.getElementsByTagName("instruction");

        for (int i = 0; i < instructionNodes.getLength(); i++) {
            Element element = (Element) instructionNodes.item(i);

            String id = element.getAttribute("id");
            byte opcode = Byte.decode(element.getAttribute("opcode"));
            int operands = Integer.parseInt(element.getAttribute("operands"));
            String conditionStr = element.getAttribute("condition");
            String handlerStr = element.getAttribute("handler");
            String description = element.getAttribute("description");

            ICondition condition = parseCondition(conditionStr);

            AbstractInstruction handler;
            if (handlerStr.contains(JumpInstruction.class.getSimpleName()))
                handler = new JumpInstruction(opcode, condition);
            else
                handler = createInstruction(parseHandler(handlerStr), opcode, operands, condition);

            set.registerInstruction(id, opcode, description, handler);
        }
    }

    private static ICondition parseCondition(String condStr) throws Exception {
        if (condStr == null || condStr.isEmpty()) return null;

        List<ConditionToken> tokens = ConditionLexer.tokenize(condStr);
        ConditionNode ast = ConditionParser.parse(tokens);

        return buildCondition(ast);
    }

    private static Class<? extends AbstractInstruction> parseHandler(String handlerStr) throws Exception {
        if (handlerStr == null || handlerStr.isEmpty()) return null;

        if (handlerStr.startsWith(CORE_IMPLEMENTATION))
            return Class.forName(CLASS_PATH_PREFIX + handlerStr.trim()).asSubclass(AbstractInstruction.class);
        return Class.forName(handlerStr.trim()).asSubclass(AbstractInstruction.class);
    }

    private static AbstractInstruction createInstruction(Class<? extends AbstractInstruction> clazz, byte opcode, int operands, ICondition condition) throws Exception {
        return clazz
                .getDeclaredConstructor(byte.class, int.class, ICondition.class)
                .newInstance(opcode, operands, condition);
    }

    private static ICondition buildCondition(ConditionNode node) throws Exception {
        if (node instanceof LeafNode(String className)) {
            Class<?> cls;
            if (className.startsWith(CORE_IMPLEMENTATION))
                cls = Class.forName(CLASS_PATH_PREFIX + className);
            else
                cls = Class.forName(className);
            return (ICondition) cls.getDeclaredConstructor().newInstance();
        }

        if (node instanceof NotNode(ConditionNode inner)) {
            return new NotCondition(buildCondition(inner));
        }

        if (node instanceof OrNode(ConditionNode left, ConditionNode right)) {
            return new OrCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        if (node instanceof AndNode(ConditionNode left, ConditionNode right)) {
            return new AndCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        throw new IllegalStateException("Unknown ConditionNow: " + node);
    }
}
