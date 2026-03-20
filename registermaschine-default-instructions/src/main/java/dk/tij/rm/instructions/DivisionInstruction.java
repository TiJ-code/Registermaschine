package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

import java.util.Arrays;

public final class DivisionInstruction extends AbstractInstruction {
    public DivisionInstruction(byte opcode, int operandCount, ICondition condition) {
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
        boolean overflow = false;
        Integer runningQuotient = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
                continue;
            }

            int currentValue = getValueFromOperand(context, op);

            if (runningQuotient == null) {
                runningQuotient = currentValue;
            } else {
                if (currentValue == 0) {
                    System.err.println("Runtime Error: Division by zero!");
                    context.setExitCode((byte) 1);
                    context.stopExecution();
                    return;
                }

                if (runningQuotient == Integer.MIN_VALUE && currentValue == 1) {
                    overflow = true;
                    runningQuotient = Integer.MIN_VALUE;
                } else {
                    runningQuotient /= currentValue;
                }
            }
        }

        if (destination != null && runningQuotient != null) {
            context.setFlags(runningQuotient < 0, runningQuotient == 0, overflow);
            context.setRegister(destination.value(), runningQuotient);
        }
    }
}
