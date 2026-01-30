package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class LoadConstantValueInstruction extends AbstractInstruction{
    public LoadConstantValueInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        super.executeInstruction(context, operands);
        context.setAccumulator( operands[0] );
    }
}
