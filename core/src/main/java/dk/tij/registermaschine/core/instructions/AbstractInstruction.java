package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public abstract class AbstractInstruction {
    public final byte OpCode;
    protected final int operandCount;
    protected final Condition condition;

    public AbstractInstruction(final byte opcode, final int operandCount, Condition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    public void executeInstruction(ExecutionContext context, int[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new IllegalArgumentException("Insufficient operands!");
    }

    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
