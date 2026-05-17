package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class MultiplicationStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long product = 1;

        for (int inputIndex : inputIndices) {
            product *= getValueFromOperand(context, operands[inputIndex]);
        }

        int intProduct = (int) product;

        boolean overflow = (product > Integer.MAX_VALUE) || (product < Integer.MIN_VALUE);

        context.setFlags(intProduct < 0, intProduct == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intProduct);
    }
}
