package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, ICondition condition) {
        super(opcode, 1, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, int[] operands) {
        context.setProgrammeCounter( operands[0] );
    }
}
