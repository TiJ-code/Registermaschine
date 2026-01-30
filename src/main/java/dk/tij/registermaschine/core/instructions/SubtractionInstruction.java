package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class SubtractionInstruction extends AbstractInstruction {
    public SubtractionInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        super.executeInstruction(context, operands);
        context.setAccumulator(context.getAccumulator() - operands[0]);
    }
}
