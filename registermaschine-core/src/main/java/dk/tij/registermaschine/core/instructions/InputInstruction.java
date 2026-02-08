package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public final class InputInstruction extends AbstractInstruction {
    public InputInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        try {
            context.setRegister(operands[0].value(), context.input());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
