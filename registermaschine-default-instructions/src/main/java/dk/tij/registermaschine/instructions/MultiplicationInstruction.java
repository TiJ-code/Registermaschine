package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

import java.util.Arrays;

public final class MultiplicationInstruction extends AbstractInstruction {
    public MultiplicationInstruction(byte opcode, int operandCount, ICondition condition) {
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
        long product = 1;
        ICompiledOperand destination = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
            } else if (op.concept() == OperandConcept.OPERAND) {
                product *= getValueFromOperand(context, op);
            }
        }

        boolean overFlow = (product > Integer.MAX_VALUE) ||
                           (product < Integer.MIN_VALUE);

        if (destination != null) {
            context.setFlags(product < 0, product == 0, overFlow);
            context.setRegister(destination.value(), (int) product);
        }
    }
}
