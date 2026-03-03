package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public final class MoveInstruction extends AbstractInstruction {
    public MoveInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        int value = 0;
        int destinationIndex = -1;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destinationIndex = op.value();
            } else {
                value = getValueFromOperand(context, op);
            }
        }

        if (destinationIndex != -1) {
            context.setRegister(destinationIndex, value);
        }
    }
}
