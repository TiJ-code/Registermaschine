package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.config.model.ConfigInstruction;
import dk.tij.registermaschine.api.config.model.ConfigOperand;
import dk.tij.registermaschine.api.config.model.ConfigStep;
import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.core.config.internal.conditions.ConditionBuilder;
import dk.tij.registermaschine.core.instructions.StepHandlerRegistry;
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
    private static final ILogger log = LoggerFactory.getLogger(InstructionParser.class);

    /**
     * Parses the global options and the complete list of instructions from the XML
     *
     * @param xmlDocument the configuration source
     */
    @Override
    public void parseConfig(Document xmlDocument) {
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

        List<ConfigStep> steps = parseChain(instructionElem);

        ICondition condition = ConditionBuilder.build(instructionConditionStr);

        return new ConfigInstruction(instructionMnemonic, instructionDescription, opcode, condition, operands, steps);
    }

    /**
     * Parses a single operand and validates its type/concept compatibility.
     *
     * @param operandNode the current operand node to parse
     * @return the parsed {@link ConfigOperand}
     */
    private static ConfigOperand parseOperand(Node operandNode) {
        Element operandElem = (Element) operandNode;

        String nameStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_NAME);
        String typeStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_TYPE).toUpperCase();
        log.debug("Parsing type {} for operandNode {}", typeStr, operandNode);

        String conceptStr = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_CONCEPT).toUpperCase();
        log.debug("Parsing concept {} for operandNode {}", conceptStr, operandNode);

        String value = operandElem.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_IMPLICIT_VALUE);
        log.debug("Parsing value {} for operandNode {}", value, operandNode);

        if (nameStr.isEmpty()) {
            throw new ConfigurationParseException("Operand %s must have a name.".formatted(operandElem));
        }

        validate(typeStr, conceptStr);

        OperandType type = OperandType.valueOf(typeStr);
        OperandConcept concept = OperandConcept.valueOf(conceptStr);

        if (value.isEmpty()) value = null;

        return new ConfigOperand(nameStr, type, concept, value);
    }

    /**
     * Parses the {@code <chain>} element of an instruction and converts it into a list
     * of {@link ConfigStep} definitions.
     *
     * <p>An instruction must define exactly one {@code <chain>} element, which contains
     * one or more {@code <step>} elements describing the execution sequence.</p>
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Exactly one {@code <chain>} element must be present</li>
     *     <li>The chain must contain at least one {@code <step>}</li>
     * </ul>
     *
     * @param instructionElement the XML element representing the instruction
     * @return a list of parsed {@link ConfigStep} objects in execution order
     * @throws ConfigurationParseException if the chain is missing, duplicated, or contains no steps
     * @throws ClassNotFoundException      if a step handler class cannot be resolved
     * @throws IllegalStateException       if handler configuration is invalid
     */
    private static List<ConfigStep> parseChain(Element instructionElement)
            throws ConfigurationParseException, ClassNotFoundException, IllegalStateException {
        NodeList chainNodes = instructionElement.getElementsByTagName(XmlConstants.TAG_CHAIN);

        if (chainNodes.getLength() == 0) {
            throw new ConfigurationParseException("Instruction %s is missing chain.".formatted(instructionElement));
        } else if (chainNodes.getLength() > 1) {
            throw new ConfigurationParseException("Instruction %s must have exactly one chain.".formatted(instructionElement));
        }

        Element chainElement = (Element) chainNodes.item(0);

        NodeList stepNodes = chainElement.getElementsByTagName(XmlConstants.TAG_STEP);
        if (stepNodes.getLength() == 0) {
            throw new ConfigurationParseException("Instruction %s is missing step.".formatted(instructionElement));
        }

        List<ConfigStep> steps = new ArrayList<>(stepNodes.getLength());
        for (int i = 0; i < stepNodes.getLength(); i++) {
            steps.add(parseStep(stepNodes.item(i)));
        }

        return steps;
    }

    /**
     * Parses a single {@code <step>} element into a {@link ConfigStep}.
     *
     * <p>A step defines a single execution unit within an instruction chain. It consists of:</p>
     * <ul>
     *     <li>A required handler class ({@code handler})</li>
     *     <li>An optional execution condition</li>
     *     <li>Zero or more input references ({@code <in ref="..."/>})</li>
     *     <li>An optional output reference ({@code <out to="..."/>})</li>
     * </ul>
     *
     * <p>The handler is resolved dynamically via reflection and cached using
     * {@link StepHandlerRegistry} to avoid repeated instantiation.</p>
     *
     * <p>Input and output references correspond to operand names defined at the
     * instruction level and are resolved later during precompilation.</p>
     *
     * @param stepNode the XML node representing the step
     * @return the parsed {@link ConfigStep}
     *
     * @throws ConfigurationParseException if the step definition is invalid
     * @throws ClassNotFoundException      if the handler class cannot be found
     * @throws IllegalStateException       if the handler attribute is missing or invalid
     */
    private static ConfigStep parseStep(Node stepNode)
            throws ConfigurationParseException, ClassNotFoundException, IllegalStateException {
        Element stepElem = (Element) stepNode;

        String handlerStr = stepElem.getAttribute(XmlConstants.ATTRIBUTE_STEP_HANDLER);
        String conditionStr = stepElem.getAttribute(XmlConstants.ATTRIBUTE_STEP_CONDITION);

        Class<? extends IStepHandler> handlerClass = parseInstructionHandler(handlerStr);

        ICondition condition = ConditionBuilder.build(conditionStr);

        NodeList children = stepElem.getChildNodes();

        List<String> inputs = new ArrayList<>(children.getLength() - 1);
        String output = null;

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            switch (element.getTagName()) {
                case XmlConstants.TAG_IN -> inputs.add(element.getAttribute(XmlConstants.ATTRIBUTE_IN_REF));
                case XmlConstants.TAG_OUT -> {
                    String outAttr = element.getAttribute(XmlConstants.ATTRIBUTE_OUT_TO);
                    output = outAttr.isEmpty() ? null : outAttr;
                }
            }
        }

        IStepHandler handler = StepHandlerRegistry.getOrCreate(handlerClass, InstructionParser::createInstructionHandler);

        return new ConfigStep(handler, condition, inputs, output);
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

    private static Class<? extends IStepHandler> parseInstructionHandler(String handlerString)
            throws IllegalStateException, ClassNotFoundException {
        if (handlerString == null || handlerString.isEmpty()) {
            throw new IllegalStateException("Cannot parse empty instruction handler.");
        }

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
