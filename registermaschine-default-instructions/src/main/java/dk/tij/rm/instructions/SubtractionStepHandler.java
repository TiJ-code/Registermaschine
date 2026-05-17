package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class SubtractionStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long difference = getValueFromOperand(context, operands[inputIndices[0]]);

        for (int i = 1; i < inputIndices.length; i++) {
            difference -= getValueFromOperand(context, operands[inputIndices[i]]);
        }

        int intDifference = (int) difference;

        boolean overflow = (difference > Integer.MAX_VALUE) || (difference < Integer.MIN_VALUE);

        context.setFlags(intDifference < 0, intDifference == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intDifference);
    }
}
