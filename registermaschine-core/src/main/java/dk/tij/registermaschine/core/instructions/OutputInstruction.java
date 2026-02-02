package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;
import dk.tij.registermaschine.core.conditions.ICondition;

public final class OutputInstruction extends AbstractInstruction {
    public OutputInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        System.out.println( context.getAccumulator() );
    }
}
