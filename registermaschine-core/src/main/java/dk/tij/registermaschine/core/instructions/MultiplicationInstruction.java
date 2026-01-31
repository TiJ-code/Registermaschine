package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class MultiplicationInstruction extends AbstractInstruction {
    public MultiplicationInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        super.executeInstruction(context, operands);

        long result = (long)context.getAccumulator() * (long)operands[0];

        boolean overFlow = result > Integer.MAX_VALUE || result < Integer.MIN_VALUE;

        context.setFlags(result < 0, (int)result == 0, overFlow);
        context.setAccumulator((int)result);
    }
}
