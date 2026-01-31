package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class DivisionInstruction extends AbstractInstruction {
    public DivisionInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        super.executeInstruction(context, operands);

        Integer result;
        try {
            result = context.getAccumulator() / operands[0];
        } catch (ArithmeticException e) {
            System.err.println("Error!");
            result = null;
        }
        context.updateFlags(operands, operandCount, result);
        context.setAccumulator(Integer.MIN_VALUE);
    }
}
