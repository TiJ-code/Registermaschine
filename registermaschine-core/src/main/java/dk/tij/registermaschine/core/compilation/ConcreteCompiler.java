package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.compiling.CompiledInstruction;
import dk.tij.registermaschine.core.compilation.parsing.InstructionNode;
import dk.tij.registermaschine.core.compilation.parsing.OperandNode;

import java.util.ArrayList;
import java.util.List;

public final class ConcreteCompiler implements ICompiler {
    @Override
    public ICompiledProgram compile(ISyntaxTree tree, InstructionSet instructionSet) {
        List<ICompiledInstruction> program = new ArrayList<>();

        for (ISyntaxTreeNode node : tree) {
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
