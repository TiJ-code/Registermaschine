package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.api.compilation.ICompiler;
import dk.tij.registermaschine.api.compilation.compiling.*;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.api.config.ConfigInstruction;
import dk.tij.registermaschine.api.config.ConfigOperand;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledOperand;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledProgram;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteAbstractSyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteLabelNode;
import dk.tij.registermaschine.core.config.*;
import dk.tij.registermaschine.api.error.SyntaxErrorException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledInstruction;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteInstructionNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteOperandNode;
import dk.tij.registermaschine.api.instructions.IInstructionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Concrete implementation of {@link ICompiler} for the Registermaschine.
 *
 * <p>This compiler transforms a parsed {@link ISyntaxTree} into a
 * {@link ICompiledProgram} that can be executed by the runtime. It resolves
 * labels, validates operand types, and converts instruction operands into
 * {@link ICompiledOperand} objects according to their {@link ConfigOperand}
 * templates.</p>
 *
 * <p>Compilation process:</p>
 * <ol>
 *     <li>First pass: scan for labels to build a symbol table</li>
 *     <li>Second pass: compile each instruction node, resolving operands and labels</li>
 *     <li>Validate each instruction using its {@link AbstractInstruction} handler</li>
 * </ol>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Supports register, immediate, and label operands</li>
 *     <li>Validates operand types against instruction templates</li>
 *     <li>Resolves label addresses and user-provided hexadecimal addresses</li>
 *     <li>Handles implicit operands defined in the configuration</li>
 *     <li>Throws {@link SyntaxErrorException} for invalid operands or registers</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConcreteCompiler implements ICompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcreteCompiler.class);

    /**
     * Compiles the given {@link ISyntaxTree} into a {@link ICompiledProgram}.
     *
     * @param tree the syntax tree produced by the parser
     * @param instructionSet the instruction set used to resolve instructions
     * @return a compiled program ready for execution
     * @throws RuntimeException for unknown instructions or operand mismatches
     */
    @Override
    public ICompiledProgram compile(ISyntaxTree tree, IInstructionSet instructionSet) {
        LOGGER.debug("Starting compilation of syntax tree");

        List<ICompiledInstruction> program = new ArrayList<>();
        Map<String, Integer> symbolTable = new HashMap<>();

        int instructionIdx = 0;
        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteLabelNode labelNode) {
                LOGGER.trace("Registering label '{}' at instruction index {}", labelNode.label(), instructionIdx);
                symbolTable.put(labelNode.label(), instructionIdx);
            } else if (node instanceof ConcreteInstructionNode) {
                instructionIdx++;
            }
        }

        LOGGER.debug("Symbol table built: {}", symbolTable);

        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteInstructionNode instr) {
                LOGGER.debug("Compiling instruction '{}' at line {}", instr.instruction(), instr.line);

                ConfigInstruction config = instructionSet.getInstructions().stream()
                        .filter(c -> c.mnemonic().equalsIgnoreCase(instr.instruction()))
                        .findFirst().orElseThrow(() -> {
                            LOGGER.error("Unknown instruction '{}'", instr.instruction());
                            return new RuntimeException("Unknown: " + instr.instruction());
                        });

                LOGGER.trace("Matched instruction config: {}", config);

                ICompiledOperand[] finalOperands = mergeOperands(config.operands(), instr.operands, symbolTable);

                AbstractInstruction handler = instructionSet.getHandler(instr.instruction());

                LOGGER.trace("Validating operands {}", Arrays.toString(finalOperands));
                handler.validate(finalOperands);

                LOGGER.trace("Instruction compiled -> opcode={}, operands={}",
                        config.opcode(), Arrays.toString(finalOperands));

                program.add(new ConcreteCompiledInstruction(config.opcode(), finalOperands));
            }
        }

        LOGGER.info("Compilation finished. Program size: {}", program.size());

        return new ConcreteCompiledProgram(program);
    }

    /**
     * Merges instruction template operands with user-provided operands.
     *
     * @param template the instruction's configured operands
     * @param userNodes the operands provided in source code
     * @param symbolTable mapping of labels to instruction addresses
     * @return an array of {@link ICompiledOperand}
     */
    private ICompiledOperand[] mergeOperands(List<ConfigOperand> template, List<ConcreteOperandNode> userNodes,
                                             Map<String, Integer> symbolTable) {
        LOGGER.trace("Merging operands: tempalte={}, userNodes={}", template, userNodes);

        ICompiledOperand[] result = new ICompiledOperand[template.size()];
        int userIdx = 0;

        for (int i = 0; i < template.size(); i++) {
            ConfigOperand t = template.get(i);

            if (t.isImplicit()) {
                LOGGER.trace("Using implicit operand at index {}: {}", i, t);
                result[i] = parseInternalValue(t);
            } else {
                if (userIdx >= userNodes.size()) {
                    LOGGER.error("Missing operand at index {}", i);
                    throw new RuntimeException("Missing operand for template at index " + i);
                }

                ConcreteOperandNode userNode = userNodes.get(userIdx++);
                LOGGER.trace("Mapping user operand {} -> template {}", userNode, t);

                if (t.type() == OperandType.REGISTER && !userNode.isRegister)
                    throw error(userNode, "Operand %d must be a REGISTER, but got IMMEDIATE", i);

                if (t.type() == OperandType.LABEL && !userNode.isAddress)
                    throw error(userNode, "Jump targets must be the '@0x' address prefix");

                if (t.type() == OperandType.IMMEDIATE && userNode.isRegister)
                    throw error(userNode, "Operand %d must be a IMMEDIATE, but got REGISTER", i);

                result[i] = parseUserValue(userNode, t.concept(), symbolTable);
            }
        }

        if (userIdx < userNodes.size()) {
            LOGGER.error("Too many operands provided: expected {}, got {}", template.size(), userNodes.size());
            throw new RuntimeException("Too many operands provided for this instruction definition.");
        }

        return result;
    }

    /**
     * Parses implicit operand values defined in the configuration.
     */
    private ICompiledOperand parseInternalValue(ConfigOperand op) {
        LOGGER.trace("Parsing internal operand: {}", op);

        ICompiledOperand result;
        String value = op.value();
        if (op.type() == OperandType.REGISTER && value.toLowerCase().startsWith("r")) {
            result = new ConcreteCompiledOperand(OperandType.REGISTER, op.concept(),
                                                 Integer.parseInt(value.substring(1)));
        } else {
            if (op.concept() == OperandConcept.TARGET && !value.startsWith("0x")) {
                LOGGER.error("Invalid internal address '{}'", value);
                throw new RuntimeException(String.format("XML Error: Address value '%s' must be hex.", value));
            }
            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(value));
        }

        LOGGER.trace("Parsed internal operand -> {}", result);
        return result;
    }

    /**
     * Parses a user-provided operand into a compiled operand, resolving labels and addresses.
     */
    private ICompiledOperand parseUserValue(ConcreteOperandNode node, OperandConcept concept, Map<String, Integer> symbolTable) {
        LOGGER.trace("Parsing user operand '{}' with concept {}", node, concept);

        ICompiledOperand result;
        if (node.isRegister) {
            int regIndex = Integer.parseInt(node.value().substring(1));

            if (regIndex >= CoreConfig.REGISTERS) {
                LOGGER.error("Invalid register index {}", regIndex);
                throw error(node, "Invalid register 'r%d'. The machine is configured with only %d registers (r0 - r%d).",
                        regIndex, CoreConfig.REGISTERS, CoreConfig.REGISTERS-1);
            }

            result = new ConcreteCompiledOperand(OperandType.REGISTER, concept, regIndex);
        } else {
            String value = node.value();

            if (concept == OperandConcept.TARGET) {
                if (value.startsWith("@")) {
                    if (!value.startsWith("@0x") && !value.startsWith("@0X")) {
                        LOGGER.error("Invalid address format '{}'", value);
                        throw error(node,
                                "Invalid address format: %s. Addresses must be hexadecimal and start with '@0x'",
                                value);
                    }

                    int addr = Integer.decode(value.substring(1)) - 1; // line number offset
                    LOGGER.trace("Resolved direct address {} -> {}", value, addr);

                    return new ConcreteCompiledOperand(OperandType.IMMEDIATE, concept, addr);
                } else {
                    if (!symbolTable.containsKey(value)) {
                        LOGGER.error("Undefined label '{}'", value);
                        throw error(node, "Undefined label: %s", value);
                    }

                    int resolveAddress = symbolTable.get(value);
                    LOGGER.trace("Resolved label '{}' -> {}", value, resolveAddress);

                    return new ConcreteCompiledOperand(OperandType.IMMEDIATE, concept, resolveAddress);
                }
            }

            if (value.startsWith("@")) {
                LOGGER.error("Illegal '@' usage in operand '{}'", value);
                throw error(node, "Address prefix '@' is not allowed for this operand type.");
            }

            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(value));
        }

        LOGGER.trace("Parsed user operand -> {}", result);
        return result;
    }

    /**
     * Constructs a {@link SyntaxErrorException} with node location and message formatting.
     */
    private SyntaxErrorException error(ConcreteAbstractSyntaxTreeNode node, String msg, Object... args) {
        String formatted = msg.formatted(args);

        LOGGER.error("Compilation error at line {}: {}", node.line, formatted);

        return new SyntaxErrorException(String.format("at line %d: %s", node.line, formatted));
    }
}
