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

        int result = context.getAccumulator() * operands[0];
        context.updateFlags(operands, operandCount, result);
        context.setAccumulator(result);
    }
}
