package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class SubtractionStepHandler implements IStepHandler {
    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long difference = 0;

        for (int inputIdx : inputIndices) {
            difference -= context.getRegister(operands[inputIdx].value());
        }

        int intDifference = (int) difference;

        boolean overflow = (difference > Integer.MAX_VALUE) || (difference < Integer.MIN_VALUE);

        context.setFlags(intDifference < 0, intDifference == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intDifference);
    }
}
