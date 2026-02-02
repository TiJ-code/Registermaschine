package dk.tij.registermaschine.core.compilation.internal;

import dk.tij.registermaschine.core.compilation.AbstractSyntaxTree;
import dk.tij.registermaschine.core.compilation.CompiledProgram;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.compiling.CompiledInstruction;
import dk.tij.registermaschine.core.compilation.parsing.AbstractSyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.parsing.InstructionNode;
import dk.tij.registermaschine.core.compilation.parsing.OperandNode;

import java.util.ArrayList;
import java.util.List;

public final class Compiler {
    private Compiler() {}

    public static CompiledProgram compile(AbstractSyntaxTree tree, InstructionSet instructionSet) {
        List<CompiledInstruction> program = new ArrayList<>();

        for (AbstractSyntaxTreeNode node : tree) {
            if (node instanceof InstructionNode instr) {
                byte opcode = instructionSet.getOpcode(instr.instruction);
                int[] operands = compileOperands(instr.operands);

                AbstractInstruction handler = instructionSet.getHandler(opcode);
                handler.validate(operands);

                program.add(new CompiledInstruction(opcode, operands));
            }
        }

        return new CompiledProgram(program);
    }

    private static int[] compileOperands(List<OperandNode> operandNodes) {
        int[] result = new int[operandNodes.size()];

        for (int i = 0; i < operandNodes.size(); i++) {
            OperandNode op = operandNodes.get(i);

            if (op.isRegister) {
                result[i] = Integer.parseInt(op.value.substring(1));
            } else {
                result[i] = Integer.decode(op.value);
            }
        }

        return result;
    }
}
