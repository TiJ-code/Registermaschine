package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public abstract class AbstractInstruction {
    public final byte OpCode;
    protected final int operandCount;
    protected final ICondition condition;

    public AbstractInstruction(final byte opcode, final int operandCount, ICondition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    public void validate(int[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new RuntimeException("Instruction " + this.getClass().getSimpleName() + " expects " + operandCount + " operands.");
    }

    public boolean shouldExecute(IExecutionContext context) {
        if (condition == null) return true;
        return condition.test(context);
    }

    public abstract void executeInstruction(IExecutionContext context, int[] operands);

    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
