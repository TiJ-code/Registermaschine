package dk.tij.registermaschine.api.compilation.compiling;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.IStepHandler;

public interface ICompiledStep {
    IStepHandler handler();
    ICondition condition();
    int[] inputIndices();
    int outputIndex();
}
