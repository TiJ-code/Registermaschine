package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandType;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.core.config.internal.conditions.ConditionBuilder;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.config.model.ConfigOperand;
import dk.tij.registermaschine.core.config.model.ConfigStep;
import dk.tij.registermaschine.core.error.ClassInstantiationException;
import dk.tij.registermaschine.core.error.ConfigurationParseException;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.instructions.internal.StepHandlerRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public final class InstructionSetParser implements IConfigParser {

    @Override
    public void parseConfig(Document xmlDocument) {
        NodeList instructionNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_INSTRUCTION);
        if (instructionNodeList.getLength() < 0) return;

        List<ConfigInstruction> instructions = new ArrayList<>(instructionNodeList.getLength());
        for (int i = 0; i < instructionNodeList.getLength(); i++) {
            Node instructionNode = instructionNodeList.item(i);
            if (instructionNode.getNodeType() != Node.ELEMENT_NODE) continue;

            try {
                ConfigInstruction instruction = parseInstruction(instructionNode, (byte) instructions.size());
                instructions.add(instruction);

                fireEvent((Element) instructionNode, instruction);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        CoreConfig.INSTRUCTIONS.addAll(instructions);
    }

    private static ConfigInstruction parseInstruction(Node instructionNode, final byte opcode)
            throws ConfigurationParseException, ClassNotFoundException, IllegalStateException {
        Element instructionElem = (Element) instructionNode;

        String instructionMnemonic = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_ID);
        String instructionDescription = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_DESCRIPTION);
        String instructionCondition = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_CONDITION);

        int resultCount = 0;

        NodeList operandNodes = instructionElem.getElementsByTagName(XmlConstants.TAG_OPERAND);
        List<ConfigOperand> operands = new ArrayList<>(operandNodes.getLength());
        for (int i = 0; i < operandNodes.getLength(); i++) {
            ConfigOperand operand = parseOperand(operandNodes.item(i));

            if (operand.concept() == OperandConcept.RESULT) {
                resultCount++;
            }

            operands.add(operand);
        }

        if (resultCount > 1) {
            throw new ConfigurationParseException(String.format("Instruction %s must have exactly one result.",
                    instructionMnemonic));
        }
        
        List<ConfigStep> steps = parseChain(instructionElem, opcode);
        
        ICondition condition = ConditionBuilder.build(instructionCondition);

        return new ConfigInstruction(instructionMnemonic, instructionDescription,
                                     opcode, condition, operands, steps);
    }

    private static ConfigOperand parseOperand(Node operandNode) {
        Element operandElem = (Element) operandNode;

        String nameStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_NAME);
        String typeStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_TYPE).toUpperCase();
        String conceptStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_CONCEPT).toUpperCase();
        String value = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_IMPLICIT_VALUE);

        if (nameStr.isEmpty())
            throw new ConfigurationParseException("Operand %s must have a name".formatted(operandElem));

        validate(typeStr, conceptStr);

        OperandType type = OperandType.valueOf(typeStr);
        OperandConcept concept = OperandConcept.valueOf(conceptStr);

        if (value.isEmpty()) value = null;

        return new ConfigOperand(nameStr, type, concept, value);
    }
    
    private static List<ConfigStep> parseChain(Element instructionElement, byte opcode)
            throws ConfigurationParseException, ClassNotFoundException, IllegalStateException {
        NodeList chainNodes = instructionElement.getElementsByTagName(XmlConstants.TAG_CHAIN);
        
        if (chainNodes.getLength() == 0) {
            throw new ConfigurationParseException("Instruction %s missing chain".formatted(instructionElement));
        } else if (chainNodes.getLength() > 1) {
            throw new ConfigurationParseException("Instruction %s must have exactly one chain.".formatted(instructionElement));
        }
        
        Element chainElement = (Element) chainNodes.item(0);
        
        NodeList stepNodes = chainElement.getElementsByTagName(XmlConstants.TAG_STEP);
        if (stepNodes.getLength() == 0) {
            throw new ConfigurationParseException("Instruction %s missing step".formatted(instructionElement));
        }
        
        List<ConfigStep> steps = new ArrayList<>(stepNodes.getLength());
        for (int i = 0; i < stepNodes.getLength(); i++) {
            steps.add(parseStep(stepNodes.item(i), opcode));
        }
        
        return steps;
    }
    
    private static ConfigStep parseStep(Node stepNode, byte opcode)
            throws ConfigurationParseException, ClassNotFoundException, IllegalStateException {
        Element stepElement = (Element) stepNode;

        String handlerStr = stepElement.getAttribute(XmlConstants.ATTRIBUTE_STEP_HANDLER);
        String conditionStr = stepElement.getAttribute(XmlConstants.ATTRIBUTE_STEP_CONDITION);

        Class<? extends IStepHandler> handlerClass = parseInstructionHandler(handlerStr);

        ICondition condition = ConditionBuilder.build(conditionStr);

        NodeList children = stepElement.getChildNodes();

        List<String> inputs = new ArrayList<>(children.getLength() - 1); // max. 1 output
        String output = null;

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element elem = (Element) node;

            switch (elem.getTagName()) {
                case XmlConstants.TAG_IN -> inputs.add(elem.getAttribute(XmlConstants.ATTRIBUTE_IN_REF));
                case XmlConstants.TAG_OUT -> {
                    String outAttr = elem.getAttribute(XmlConstants.ATTRIBUTE_OUT_TO);
                    output = outAttr.isEmpty() ? null : outAttr;
                }
            }
        }

        IStepHandler handler = StepHandlerRegistry.getHandler(handlerClass);
        if (handler == null) {
            handler = createInstructionHandler(handlerClass);
            StepHandlerRegistry.registerHandler(handler);
        }

        return new ConfigStep(handler, condition, inputs, output);
    }

    private static void validate(String type, String concept) throws ConfigurationParseException {
        OperandType parsedType;
        OperandConcept parsedConcept;
        try {
            parsedType = OperandType.valueOf(type);
            parsedConcept = OperandConcept.valueOf(concept);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationParseException("Invalid value: " + e.getMessage());
        }
        
        boolean isIllegal = switch (parsedConcept) {
            case OperandConcept.RESULT -> parsedType != OperandType.REGISTER;
            case OperandConcept.OPERAND -> parsedType == OperandType.LABEL;
            case OperandConcept.TARGET -> parsedType != OperandType.LABEL;
        };

        if (isIllegal) {
            throw new ConfigurationParseException(
                    String.format("Invalid combination: type=%s with concept=%s", parsedType, parsedConcept)
            );
        }
    }

    private static Class<? extends IStepHandler> parseInstructionHandler(String handlerString)
            throws IllegalStateException, ClassNotFoundException {
        if (handlerString == null || handlerString.isEmpty())
            throw new IllegalStateException("Cannot parse empty instruction handler");

        if (handlerString.startsWith(CoreConfig.CORE_IMPLEMENTATION_PREFIX))
            return Class.forName(CoreConfig.CORE_CLASS_PATH_PREFIX + handlerString.trim()).asSubclass(IStepHandler.class);
        else
            return Class.forName(handlerString.trim()).asSubclass(IStepHandler.class);
    }

    private static IStepHandler createInstructionHandler(Class<? extends IStepHandler> handlerClass)
            throws ClassInstantiationException {
        try {
            return handlerClass
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new ClassInstantiationException("Could not instantiate instruction handler class.", e);
        }
    }
}
