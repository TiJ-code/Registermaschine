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

        int result = context.getAccumulator() - operands[0];

        boolean negative = result < 0;
        boolean overFlow = ((context.getAccumulator() ^ operands[0]) & (context.getAccumulator() ^ result)) < 0;

        context.setFlags(negative, result == 0, overFlow);

        context.setAccumulator(result);
    }
}
