package dk.tij.registermaschine.core.compilation.internal.instructions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;

public record CompiledStep(AbstractInstruction handler, int[] inputIndices, int outputIndex, ICondition condition) {}
