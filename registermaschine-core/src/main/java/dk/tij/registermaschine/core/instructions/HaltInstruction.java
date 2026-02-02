package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;
import dk.tij.registermaschine.core.conditions.ICondition;

public final class HaltInstruction extends AbstractInstruction {
    public HaltInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        context.setExitCode((byte)operands[0]);
        context.stopExecution();
    }
}
