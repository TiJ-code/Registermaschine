package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.IStepHandler;

public record ConcreteCompiledStep(IStepHandler handler, ICondition condition,
                                   int[] inputIndices, int outputIndex)
        implements ICompiledStep {}
