package tij.registermaschine.core.compilation.internal.compiling;

import tij.registermaschine.api.compilation.compiling.ICompiledStep;
import tij.registermaschine.api.conditions.ICondition;
import tij.registermaschine.api.instructions.IStepHandler;

public record ConcreteCompiledStep(IStepHandler handler, ICondition condition,
                                   int[] inputIndices, int outputIndex)
        implements ICompiledStep {}
