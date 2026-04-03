package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public class NoOperationStepHandler implements IStepHandler {
    @Override
    public int requiredInputs() {
        return 0;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {}
}
