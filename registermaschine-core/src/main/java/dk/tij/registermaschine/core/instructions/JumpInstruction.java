package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;
import dk.tij.registermaschine.core.conditions.ICondition;

public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, ICondition condition) {
        super(opcode, 1, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        context.setProgrammeCounter( operands[0] );
    }
}
