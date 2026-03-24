package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

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
