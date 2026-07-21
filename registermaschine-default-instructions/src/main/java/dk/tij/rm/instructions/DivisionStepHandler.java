package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class DivisionStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long quotient = getValueFromOperand(context, operands[inputIndices[0]]);

        boolean overflow = false;

        for (int i = 1; i < inputIndices.length; i++) {
            int currentInput = getValueFromOperand(context, operands[inputIndices[i]]);

            if (currentInput == 0) {
                System.err.println("Runtime Error: Division by zero!");
                context.setExitCode((byte) 1);
                context.stopExecution();
                return;
            }

            if (quotient == Integer.MIN_VALUE && currentInput == 1)
                overflow = true;

            quotient /= currentInput;
        }

        int intQuotient = (int) quotient;

        context.setFlags(intQuotient < 0, intQuotient == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intQuotient);
    }
}
