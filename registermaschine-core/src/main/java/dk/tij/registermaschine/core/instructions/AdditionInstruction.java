package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.config.ConfigOperand;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

import java.util.Arrays;

public final class AdditionInstruction extends AbstractInstruction {
    public AdditionInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void validate(ICompiledOperand[] operands) {
        super.validate(operands);
        if (Arrays.stream(operands).noneMatch(o -> o.concept() == ConfigOperand.Concept.RESULT))
            throw new RuntimeException(String.format("Instruction Handler %s expects 1 result operand",
                    this.getClass().getSimpleName()));
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        long sum = 0;
        ICompiledOperand destination = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == ConfigOperand.Concept.RESULT) {
                destination = op;
            } else if (op.concept() == ConfigOperand.Concept.OPERAND) {
                sum += getValueFromOperand(context, op);
            }
        }

        boolean overFlow = (sum > Integer.MAX_VALUE) ||
                           (sum < Integer.MIN_VALUE);

        if (destination != null) {
            context.setFlags(sum < 0, sum == 0, overFlow);
            context.setRegister(destination.value(), (int) sum);
        }
    }
}
