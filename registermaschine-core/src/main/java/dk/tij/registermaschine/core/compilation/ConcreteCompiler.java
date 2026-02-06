package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.compiling.*;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledOperand;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledProgram;
import dk.tij.registermaschine.core.config.*;
import dk.tij.registermaschine.core.error.SyntaxErrorException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledInstruction;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteInstructionNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteOperandNode;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.ArrayList;
import java.util.List;

public final class ConcreteCompiler implements ICompiler {
    @Override
    public ICompiledProgram compile(ISyntaxTree tree, IInstructionSet instructionSet) {
        List<ICompiledInstruction> program = new ArrayList<>();

        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteInstructionNode instr) {
                ConfigInstruction config = instructionSet.getInstructions().stream()
                        .filter(c -> c.mnemonic().equalsIgnoreCase(instr.instruction()))
                        .findFirst().orElseThrow(() -> new RuntimeException("Unknown: " + instr.instruction()));

                ICompiledOperand[] finalOperands = mergeOperands(config.operands(), instr.operands);

                AbstractInstruction handler = instructionSet.getHandler(instr.instruction());
                handler.validate(finalOperands);

                program.add(new ConcreteCompiledInstruction(config.opcode(), finalOperands));
            }
        }

        return new ConcreteCompiledProgram(program);
    }

    private ICompiledOperand[] mergeOperands(List<ConfigOperand> template, List<ConcreteOperandNode> userNodes) {
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
                    throw new SyntaxErrorException("Operand " + i + " must be a REGISTER, gut got IMMEDIATE");
                if (t.type() == OperandType.IMMEDIATE && userNode.isRegister)
                    throw new SyntaxErrorException("Operand " + i + " must be a IMMEDIATE, gut got REGISTER");

                result[i] = parseUserValue(userNode, t.concept());
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
            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(value));
        }
        return result;
    }

    private ICompiledOperand parseUserValue(ConcreteOperandNode node, OperandConcept concept) {
        ICompiledOperand result;
        if (node.isRegister) {
            result = new ConcreteCompiledOperand(OperandType.REGISTER, concept,
                                                 Integer.parseInt(node.value().substring(1)));
        } else {
            result = new ConcreteCompiledOperand(OperandType.IMMEDIATE, OperandConcept.OPERAND,
                                                 Integer.decode(node.value()));
        }
        return result;
    }
}
