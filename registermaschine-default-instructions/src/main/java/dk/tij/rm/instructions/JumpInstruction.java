package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, 1, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        context.setProgrammeCounter( operands[0].value() );
    }
}
