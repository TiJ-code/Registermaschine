package dk.tij.registermaschine.core;

import dk.tij.registermaschine.core.config.InstructionRegistry;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.CompiledInstruction;
import dk.tij.registermaschine.core.parser.ast.ASTNode;
import dk.tij.registermaschine.core.parser.ast.InstructionNode;
import dk.tij.registermaschine.core.parser.ast.OperandNode;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private final InstructionRegistry registry;

    public Compiler(InstructionRegistry registry) {
        this.registry = registry;
    }

    public List<CompiledInstruction> compile(List<ASTNode> nodes) {
        List<CompiledInstruction> program = new ArrayList<>();

        for (ASTNode node : nodes) {
            if (node instanceof InstructionNode instr) {
                byte opcode = registry.getOpcode(instr.instruction);
                int[] operands = compileOperands(instr.operands);

                AbstractInstruction handler = registry.getHandler(opcode);
                handler.validate(operands);

                program.add(new CompiledInstruction(opcode, operands));
            }
        }

        return program;
    }

    private int[] compileOperands(List<OperandNode> operandNodes) {
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
