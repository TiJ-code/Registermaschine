package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.compiling.*;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.compiling.*;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteAbstractSyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteLabelNode;
import dk.tij.registermaschine.core.config.*;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.config.model.ConfigOperand;
import dk.tij.registermaschine.core.error.SyntaxErrorException;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteInstructionNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteOperandNode;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.*;

public final class ConcreteCompiler implements ICompiler {
    @Override
    public ICompiledProgram compile(ISyntaxTree tree, IInstructionSet instructionSet) {
        List<ICompiledInstruction> program = new ArrayList<>();
        Map<String, Integer> symbolTable = new HashMap<>();

        int instructionIdx = 0;
        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteLabelNode labelNode) {
                symbolTable.put(labelNode.label(), instructionIdx);
            } else if (node instanceof ConcreteInstructionNode) {
                instructionIdx++;
            }
        }

        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteInstructionNode instr) {
                ConfigInstruction config = instructionSet.getConfigInstructions().stream()
                        .filter(c -> c.mnemonic().equalsIgnoreCase(instr.instruction()))
                        .findFirst().orElseThrow(() -> new RuntimeException("Unknown: " + instr.instruction()));

                ICompiledOperand[] finalOperands = mergeOperands(config.operands(), instr.operands, symbolTable);

                program.add(new ConcreteCompiledInstruction(config.opcode(),
                                                            instructionSet.get(config.opcode()).steps(),
                                                            finalOperands));
            }
        }

        return new ConcreteCompiledProgram(program);
    }

    private ICompiledOperand[] mergeOperands(List<ConfigOperand> template, List<ConcreteOperandNode> userNodes,
                                             Map<String, Integer> symbolTable) {
        ICompiledOperand[] result = new ICompiledOperand[template.size()];
        int userIdx = 0;

        for (int i = 0; i < template.size(); i++) {
            ConfigOperand t = template.get(i);

            if (t.isImplicit()) {
                result[i] = parseInternalValue(t);
            } else {
                if (userIdx >= userNodes.size()) {
                    throw new RuntimeException("Missing operand for template at index " + i);
                }

                ConcreteOperandNode userNode = userNodes.get(userIdx++);
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
            throw new RuntimeException("Too many operands provided for this instruction definition.");
        }

        return result;
    }

    private ICompiledOperand parseInternalValue(ConfigOperand op) {
        ICompiledOperand result;
        String value = op.value();
        if (op.type() == OperandType.REGISTER && value.toLowerCase().startsWith("r")) {
            result = new ConcreteCompiledOperand(OperandType.REGISTER, op.concept(),
                                                 Integer.parseInt(value.substring(1)));
        } else {
            if (op.concept() == OperandConcept.TARGET && !value.startsWith("0x")) {
                throw new RuntimeException(String.format("XML Error: Address value '%s' must be hex.", value));
            }
            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(value));
        }
        return result;
    }

    private ICompiledOperand parseUserValue(ConcreteOperandNode node, OperandConcept concept, Map<String, Integer> symbolTable) {
        ICompiledOperand result;
        if (node.isRegister) {
            int regIndex = Integer.parseInt(node.value().substring(1));

            if (regIndex >= CoreConfig.REGISTERS) {
                throw error(node, "Invalid register 'r%d'. The machine is configured with only %d registers (r0 - r%d).",
                        regIndex, CoreConfig.REGISTERS, CoreConfig.REGISTERS-1);
            }

            result = new ConcreteCompiledOperand(OperandType.REGISTER, concept, regIndex);
        } else {
            String value = node.value();

            if (concept == OperandConcept.TARGET) {
                if (value.startsWith("@")) {
                    if (!value.startsWith("@0x") && !value.startsWith("@0X")) {
                        throw error(node,
                                "Invalid address format: %s. Addresses must be hexadecimal and start with '@0x'",
                                value);
                    }

                    int addr = Integer.decode(value.substring(1)) - 1; // line number offset
                    return new ConcreteCompiledOperand(OperandType.IMMEDIATE, concept, addr);
                } else {
                    if (!symbolTable.containsKey(value)) {
                        throw error(node, "Undefined label: %s", value);
                    }

                    int resolveAddress = symbolTable.get(value);
                    return new ConcreteCompiledOperand(OperandType.IMMEDIATE, concept, resolveAddress);
                }
            }

            if (value.startsWith("@")) {
                throw error(node, "Address prefix '@' is not allowed for this operand type.");
            }

            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(value));
        }
        return result;
    }

    private SyntaxErrorException error(ConcreteAbstractSyntaxTreeNode node, String msg, Object... args) {
        return new SyntaxErrorException(String.format("at line %d: %s", node.line, String.format(msg, args)));
    }
}
