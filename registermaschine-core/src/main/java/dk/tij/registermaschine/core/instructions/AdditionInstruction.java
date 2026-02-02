package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class AdditionInstruction extends AbstractInstruction {
    public AdditionInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        int op1 = context.getAccumulator();
        int op2 = context.getRegister(operands[0]);

        long result = (long)op1 + (long)op2;

        boolean negative = result < 0;
        boolean overFlow = (result > Integer.MAX_VALUE) ||
                           (result < Integer.MIN_VALUE);

        context.setFlags(negative, result == 0, overFlow);

        context.setAccumulator((int)result);
    }
}
