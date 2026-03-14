package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class OutputStepHandler implements IStepHandler {
    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        for (int inputIdx : inputIndices) {
            context.output(context.getRegister(operands[inputIdx].value()));
        }
    }
}
