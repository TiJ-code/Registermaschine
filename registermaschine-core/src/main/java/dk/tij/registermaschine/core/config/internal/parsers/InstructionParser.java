package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.config.ConfigInstruction;
import dk.tij.registermaschine.api.config.ConfigOperand;
import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.log.Logger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.core.config.internal.conditions.ConditionBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * The primary parser for defining the machine's instruction set and global options.
 *
 * <p>This class performs three major tasks:</p>
 * <ol>
 *     <li>Configures global flags (like allowing labels.)</li>
 *     <li>Validates the semantic relationship between operand types and concepts.</li>
 *     <li>Dynamically instantiates instruction handlers via reflection</li>
 * </ol>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class InstructionParser implements IConfigParser {
    private static final Logger log = LoggerFactory.getLogger(InstructionParser.class);

    /**
     * Parses the global options and the complete list of instructions from the XML
     *
     * @param xmlDocument the configuration source
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        log.info("Started parsing instructions");

        log.info("Parsing options for this instruction set");
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

        NodeList instructionNodeList = xmlDocument.getElementsByTagName(XmlConstants.TAG_INSTRUCTION);
        if (instructionNodeList.getLength() < 0) return;

        List<ConfigInstruction> instructions = new ArrayList<>(instructionNodeList.getLength());
        for (int i = 0; i < instructionNodeList.getLength(); i++) {
            log.debug("Parsing <{}> tag {}", XmlConstants.TAG_INSTRUCTION, i);

            Node instructionNode = instructionNodeList.item(i);
            if (instructionNode.getNodeType() != Node.ELEMENT_NODE) continue;

            try {
                ConfigInstruction instruction = parseInstruction(instructionNode, instructions.size());
                instructions.add(instruction);
                log.debug("Successfully parsed <{}> tag {}: {}", XmlConstants.TAG_INSTRUCTION, i, instruction.mnemonic());

                fireEvent((Element) instructionNode, instruction);
            } catch (Exception e) {
                log.error("Error parsing <{}> tag {}: {}", XmlConstants.TAG_INSTRUCTION, i, e.getMessage());
            }
        }

        log.debug("Moving all parsed instructions to {}", CoreConfig.class.getSimpleName());
        CoreConfig.INSTRUCTIONS.addAll(instructions);
    }

    /**
     * Parses a single instruction element, its operands, and its logic handler
     *
     * @param instructionNode the current instruction node to parse
     * @param opcode the opcode of this instruction
     * @return the parsed {@link ConfigInstruction}
     * @throws Exception if the instruction handler could not be parsed
     */
    private static ConfigInstruction parseInstruction(Node instructionNode, final int opcode)
            throws Exception {
        Element instructionElem = (Element) instructionNode;
        NodeList operandNodes = instructionElem.getElementsByTagName(XmlConstants.TAG_OPERAND);

        String instructionMnemonic = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_ID);
        log.debug("Parsing mnemonic {} for instructionNode {}", instructionMnemonic, instructionNode);

        String instructionDescription = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_DESCRIPTION);
        log.debug("Parsing description {} for instructionNode {}", instructionDescription, instructionNode);

        String instructionHandlerStr = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_HANDLER);
        log.debug("Parsing handler {} for instructionNode {}", instructionHandlerStr, instructionNode);

        String instructionConditionStr = instructionElem.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_CONDITION);
        log.debug("Parsing condition {} for instructionNode {}", instructionConditionStr, instructionNode);

        int resultCount = 0;

        List<ConfigOperand> operands = new ArrayList<>(operandNodes.getLength());
        for (int i = 0; i < operandNodes.getLength(); i++) {
            log.debug("Parsing <{}> tag {}", XmlConstants.TAG_OPERAND, i);
            ConfigOperand operand = parseOperand(operandNodes.item(i));

            if (operand.concept() == OperandConcept.RESULT) {
                log.debug("Parsed <{}> tag {} as concept {}", XmlConstants.TAG_OPERAND, i, OperandConcept.RESULT);
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

    /**
     * Parses a single operand and validates its type/concept compatibility.
     *
     * @param operandNode the current operand node to parse
     * @return the parsed {@link ConfigOperand}
     */
    private static ConfigOperand parseOperand(Node operandNode) {
        Element operandElem = (Element) operandNode;

        String typeStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_TYPE).toUpperCase();
        log.debug("Parsing type {} for operandNode {}", typeStr, operandNode);

        String conceptStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_CONCEPT).toUpperCase();
        log.debug("Parsing concept {} for operandNode {}", conceptStr, operandNode);

        String value = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_IMPLICIT_VALUE);
        log.debug("Parsing value {} for operandNode {}", value, operandNode);

        validate(typeStr, conceptStr);

        OperandType type = OperandType.valueOf(typeStr);
        OperandConcept concept = OperandConcept.valueOf(conceptStr);

        if (value.isEmpty()) value = null;

        return new ConfigOperand(type, concept, value);
    }

    /**
     * Enforces the architectural constraints of the register machine.
     *
     * <ul>
     *     <li>{@link OperandConcept#RESULT concept}: Must be a {@link OperandType#REGISTER} (cannot store a result in a literal)</li>
     *     <li>{@link OperandConcept#OPERAND concept}: Cannot be a {@link OperandType#LABEL} (labels are for jumps, not data)</li>
     *     <li>{@link OperandConcept#TARGET concept}: Must be a {@link OperandType#LABEL} (defines jump destinations)</li>
     * </ul>
     *
     * @param type the {@link OperandType} to validate
     * @param concept the {@link OperandConcept} to validate
     * @throws ConfigurationParseException if any combination of type and concept are illegal
     */
    private static void validate(String type, String concept) throws ConfigurationParseException {
        OperandType parsedType;
        OperandConcept parsedConcept;
        try {
            parsedType = OperandType.valueOf(type);
            parsedConcept = OperandConcept.valueOf(concept);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationParseException("Invalid value: " + e.getMessage());
        }

        log.debug("Validating operand type \"{}\" and concept \"{}\" combination", type, concept);
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

    /**
     * Loads the handler class from the provided string.
     *
     * @param handlerString the raw handler string
     * @return the {@link Class} of the handler
     * @throws IllegalStateException if the handler string is {@code null} or empty
     * @throws ClassNotFoundException if the handler class could not be found
     */
    private static Class<? extends AbstractInstruction> parseInstructionHandler(String handlerString)
            throws IllegalStateException, ClassNotFoundException {
        if (handlerString == null || handlerString.isEmpty())
            throw new IllegalStateException("Cannot parse empty instruction handler");

        log.debug("Parsing instruction handler {}; looking on the classpath", handlerString);
        return Class.forName(handlerString.trim()).asSubclass(AbstractInstruction.class);
    }

    /**
     * Creates a handler class instance
     *
     * @param handlerClass the class to create an instance from
     * @param opcode the opcode of this instruction
     * @param operands the operands of this instruction
     * @param condition the condition of this instruction
     * @return an instantiated {@link AbstractInstruction}
     * @throws ClassInstantiationException if the handler class could not be instantiated
     */
    private static AbstractInstruction createInstructionHandler(Class<? extends AbstractInstruction> handlerClass,
                                                                int opcode, int operands, ICondition condition)
            throws ClassInstantiationException {
        try {
            log.debug("Instantiating instruction handler {}", handlerClass.getName());
            return handlerClass
                    .getDeclaredConstructor(int.class, int.class, ICondition.class)
                    .newInstance(opcode, operands, condition);
        } catch (Exception e) {
            throw new ClassInstantiationException("Could not instantiate instruction handler class.", e);
        }
    }
}
