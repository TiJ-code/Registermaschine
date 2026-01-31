package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public abstract class AbstractInstruction implements InstructionHandler {
    public final byte OpCode;
    protected final int operandCount;
    protected final Condition condition;

    public AbstractInstruction(final byte opcode, final int operandCount, Condition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    public void validate(int[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new RuntimeException("Instruction " + this.getClass().getSimpleName() + " expects " + operandCount + " operands.");
    }

    public boolean shouldExecute(ExecutionContext context) {
        if (condition == null) return true;
        return condition.test(context);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        if (!shouldExecute(context)) context.setProgrammeCounter(context.getProgrammeCounter() + 1);
    }

    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
