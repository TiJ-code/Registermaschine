package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class LoadFromAddressInstruction extends AbstractInstruction {
    public LoadFromAddressInstruction(byte opcode, int operandCount, Condition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        super.executeInstruction(context, operands);
        context.setFlags(operands[0] < 0, operands[0] == 0, false);
        context.setAccumulator(context.getRegister(operands[0]));
    }
}
