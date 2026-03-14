package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class JumpStepHandler implements IStepHandler {
    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        context.setProgrammeCounter(operands[inputIndices[0]].value());
    }
}
