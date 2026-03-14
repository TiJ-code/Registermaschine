package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;

public record ConcreteCompiledStep(IStepHandler handler, ICondition condition,
                                   int[] inputIndices, int outputIndex)
       implements ICompiledStep {}
