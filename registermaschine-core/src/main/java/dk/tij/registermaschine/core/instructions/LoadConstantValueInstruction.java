package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class LoadConstantValueInstruction extends AbstractInstruction {
    public LoadConstantValueInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, int[] operands) {
        context.setFlags(operands[0] < 0, operands[0] == 0, false);
        context.setAccumulator( operands[0] );
    }
}
