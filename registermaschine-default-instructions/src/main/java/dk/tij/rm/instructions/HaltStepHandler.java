package dk.tij.rm.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class HaltStepHandler implements IStepHandler {
    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        context.setExitCode((byte) getValueFromOperand(context, operands[inputIndices[0]]));
        context.stopExecution();
    }
}
