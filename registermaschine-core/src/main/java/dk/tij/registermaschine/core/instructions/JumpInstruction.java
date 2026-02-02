package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;
import dk.tij.registermaschine.core.conditions.Condition;

public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, Condition condition) {
        super(opcode, 1, condition);
    }

    @Override
    public void executeInstruction(ExecutionContext context, int[] operands) {
        context.setProgrammeCounter( operands[0] );
    }
}
