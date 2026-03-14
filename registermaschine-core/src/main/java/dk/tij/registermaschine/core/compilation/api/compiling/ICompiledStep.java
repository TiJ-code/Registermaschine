package dk.tij.registermaschine.core.compilation.api.compiling;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;

public interface ICompiledStep {
    IStepHandler handler();
    ICondition condition();
    int[] inputIndices();
    int outputIndex();
}
