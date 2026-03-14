package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class AdditionStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long sum = 0;

        for (int inputIdx : inputIndices) {
            sum += context.getRegister(operands[inputIdx].value());
        }

        int intSum = (int) sum;

        boolean overflow = (sum > Integer.MAX_VALUE) || (sum < Integer.MIN_VALUE);

        context.setFlags(intSum < 0, intSum == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intSum);
    }
}
