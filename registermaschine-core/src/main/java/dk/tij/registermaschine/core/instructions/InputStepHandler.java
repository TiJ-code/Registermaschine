package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class InputStepHandler implements IStepHandler {
    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        try {
            context.setRegister(operands[outputIndex].value(), context.input());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
