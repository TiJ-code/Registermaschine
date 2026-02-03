package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class SubtractionInstruction extends AbstractInstruction {
    public SubtractionInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, int[] operands) {
        int op1 = context.getAccumulator();
        int op2 = context.getRegister(operands[0]);

        int result = op1 - op2;

        boolean negative = result < 0;
        boolean overFlow = ((op1 ^ op2) & (op1 ^ result)) < 0;

        context.setFlags(negative, result == 0, overFlow);

        context.setAccumulator(result);
    }
}
