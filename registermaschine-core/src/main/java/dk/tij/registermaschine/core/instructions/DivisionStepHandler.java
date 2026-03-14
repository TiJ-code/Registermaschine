package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class DivisionStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 2;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        long quotient = operands[inputIndices[0]].value();

        boolean overflow = false;

        for (int i = 1; i < inputIndices.length; i++) {
            int currentInput = operands[inputIndices[i]].value();
            if (currentInput == 0) {
                System.err.println("Runtime Error: Division by zero!");
                context.setExitCode((byte) 1);
                context.stopExecution();
                return;
            }

            if (quotient == Integer.MIN_VALUE && currentInput == 1) {
                overflow = true;
            } else {
                quotient /= currentInput;
            }
        }

        int intQuotient = (int) quotient;

        context.setFlags(intQuotient < 0, intQuotient == 0, overflow);
        context.setRegister(operands[outputIndex].value(), intQuotient);
    }
}
