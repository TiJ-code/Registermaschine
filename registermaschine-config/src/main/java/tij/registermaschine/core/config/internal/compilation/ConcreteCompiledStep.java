package tij.registermaschine.core.config.internal.compilation;

import tij.registermaschine.api.compilation.compiling.ICompiledStep;
import tij.registermaschine.api.conditions.ICondition;
import tij.registermaschine.api.instructions.IStepHandler;

public record ConcreteCompiledStep(IStepHandler handler, ICondition condition,
                                   int[] inputIndices, int outputIndex)
        implements ICompiledStep {}
