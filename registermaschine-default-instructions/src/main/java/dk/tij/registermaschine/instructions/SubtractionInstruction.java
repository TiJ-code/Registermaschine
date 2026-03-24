package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

import java.util.Arrays;

public final class SubtractionInstruction extends AbstractInstruction {
    public SubtractionInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void validate(ICompiledOperand[] operands) {
        super.validate(operands);
        if (Arrays.stream(operands).noneMatch(o -> o.concept() == OperandConcept.RESULT))
            throw new RuntimeException(String.format("Instruction Handler %s expects 1 result operand",
                    this.getClass().getSimpleName()));
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        ICompiledOperand destination = null;
        Long runningDifference = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
                continue;
            }

            int value = getValueFromOperand(context, op);

            if (runningDifference == null) {
                runningDifference = (long) value;
            } else {
                runningDifference -= value;
            }
        }

        if (destination != null && runningDifference != null) {
            boolean overFlow = (runningDifference > Integer.MAX_VALUE) ||
                    (runningDifference < Integer.MIN_VALUE);

            context.setFlags(runningDifference < 0, runningDifference == 0, overFlow);
            context.setRegister(destination.value(), runningDifference.intValue());
        }
    }
}
