package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public interface IInstruction {
    void execute(IExecutionContext context, ICompiledOperand[] operands);

    int operandCount();
    ICondition condition();
    ICompiledStep[] steps();
}
