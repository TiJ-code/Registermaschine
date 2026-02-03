package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class LoadFromAddressInstruction extends AbstractInstruction {
    public LoadFromAddressInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, int[] operands) {
        context.setFlags(operands[0] < 0, operands[0] == 0, false);
        context.setAccumulator(context.getRegister(operands[0]));
    }
}
