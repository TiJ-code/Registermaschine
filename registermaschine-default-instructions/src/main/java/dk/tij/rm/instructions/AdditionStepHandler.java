package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class AdditionStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long sum = 0;

        for (int inputIndex : inputIndices) {
            sum += getValueFromOperand(context, operands[inputIndex]);
        }

        int intSum = (int) sum;

        boolean overflow = (sum > Integer.MAX_VALUE) || (sum < Integer.MIN_VALUE);

        context.setFlags(intSum < 0, intSum == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intSum);
    }
}
