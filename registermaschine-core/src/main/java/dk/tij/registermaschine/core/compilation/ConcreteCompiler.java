package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledProgram;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
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
                byte opcode = instructionSet.getOpcode(instr.instruction());
                int[] operands = compileOperands(instr.operands);

                AbstractInstruction handler = instructionSet.getHandler(opcode);
                handler.validate(operands);

                program.add(new ConcreteCompiledInstruction(opcode, operands));
            }
        }

        return new ConcreteCompiledProgram(program);
    }

    private static int[] compileOperands(List<ConcreteOperandNode> operandNodes) {
        int[] result = new int[operandNodes.size()];

        for (int i = 0; i < operandNodes.size(); i++) {
            ConcreteOperandNode op = operandNodes.get(i);

            if (op.isRegister) {
                result[i] = Integer.parseInt(op.value().substring(1));
            } else {
                result[i] = Integer.decode(op.value());
            }
        }

        return result;
    }
}
