package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public class NoOperationInstruction extends AbstractInstruction {
    public NoOperationInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, 0, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        return;
    }
}
