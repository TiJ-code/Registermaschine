package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.config.ConfigInstruction;
import dk.tij.registermaschine.api.config.ConfigOperand;
import dk.tij.registermaschine.core.config.*;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.core.config.internal.conditions.ConditionBuilder;
import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public final class InstructionParser implements IConfigParser {

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
            }
        }

        CoreConfig.INSTRUCTIONS.addAll(instructions);
    }

    private static ConfigInstruction parseInstruction(Node instructionNode, final byte opcode)
            throws Exception {
        Element instructionElem = (Element) instructionNode;
        NodeList operandNodes = instructionElem.getElementsByTagName(XmlConstants.TAG_OPERAND);

        String instructionMnemonic = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_ID);
        String instructionDescription = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_DESCRIPTION);
        String instructionHandlerStr = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_HANDLER);
        String instructionConditionStr = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_CONDITION);

        int resultCount = 0;

        List<ConfigOperand> operands = new ArrayList<>(operandNodes.getLength());
        for (int i = 0; i < operandNodes.getLength(); i++) {
            ConfigOperand operand = parseOperand(operandNodes.item(i));

            if (operand.concept() == OperandConcept.RESULT) {
                resultCount++;
            }

            operands.add(operand);
        }

        if (resultCount < 0 || resultCount > 1) {
            throw new ConfigurationParseException(String.format("Instruction %s must have exactly one result.",
                    instructionMnemonic));
        }

        AbstractInstruction instructionHandler = createInstructionHandler(parseInstructionHandler(instructionHandlerStr),
                                                                          opcode, operands.size(),
                                                                          ConditionBuilder.build(instructionConditionStr));

        return new ConfigInstruction(instructionMnemonic, instructionDescription,
                                     opcode, operands, instructionHandler);
    }

    private static ConfigOperand parseOperand(Node operandNode) {
        Element operandElem = (Element) operandNode;

        String typeStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_TYPE).toUpperCase();
        String conceptStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_CONCEPT).toUpperCase();
        String value = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_IMPLICIT_VALUE);

        validate(typeStr, conceptStr);

        OperandType type = OperandType.valueOf(typeStr);
        OperandConcept concept = OperandConcept.valueOf(conceptStr);

        if (value.isEmpty()) value = null;

        return new ConfigOperand(type, concept, value);
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

    private static Class<? extends AbstractInstruction> parseInstructionHandler(String handlerString) throws  Exception {
        if (handlerString == null || handlerString.isEmpty())
            throw new IllegalStateException("Cannot parse empty instruction handler");

        return Class.forName(handlerString.trim()).asSubclass(AbstractInstruction.class);
    }

    private static AbstractInstruction createInstructionHandler(Class<? extends AbstractInstruction> handlerClass,
                                                                byte opcode, int operands, ICondition condition)
            throws ClassInstantiationException {
        try {
            return handlerClass
                    .getDeclaredConstructor(byte.class, int.class, ICondition.class)
                    .newInstance(opcode, operands, condition);
        } catch (Exception e) {
            throw new ClassInstantiationException("Could not instantiate instruction handler class.", e);
        }
    }
}
