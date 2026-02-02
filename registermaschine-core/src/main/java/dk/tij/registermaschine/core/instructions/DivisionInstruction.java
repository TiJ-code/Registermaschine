package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;
import dk.tij.registermaschine.core.conditions.ICondition;

public final class DivisionInstruction extends AbstractInstruction {
    public DivisionInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        int dividend = context.getAccumulator();
        int divisor = operands[0];

        if (divisor == 0) {
            context.setExitCode((byte) 1);
            context.stopExecution();
            System.err.println("Runtime Error: Division by zero!");
            return;
        }

        boolean overflow = (dividend == Integer.MAX_VALUE && divisor == -1);

        int result;
        if (overflow)
            result = Integer.MIN_VALUE;
        else
            result = dividend / divisor;

        context.setFlags(result < 0, result == 0, overflow);

        context.setAccumulator(Integer.MIN_VALUE);
    }
}
