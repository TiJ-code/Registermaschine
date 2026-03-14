package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class MultiplicationStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long product = 1;

        for (int inputIndex : inputIndices) {
            product *= operands[inputIndex].value();
        }

        int intProduct = (int) product;

        boolean overflow = (product > Integer.MAX_VALUE) || (product < Integer.MIN_VALUE);

        context.setFlags(intProduct < 0, intProduct == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intProduct);
    }
}
