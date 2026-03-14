package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public interface IStepHandler {
    void execute(IExecutionContext context, ICompiledOperand[] operands,
                 int[] inputIndices, int outputIndex);
}
