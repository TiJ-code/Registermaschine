package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, 1, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        context.setProgrammeCounter( getValueFromOperand(context, operands[0]) - 1 );
    }
}
