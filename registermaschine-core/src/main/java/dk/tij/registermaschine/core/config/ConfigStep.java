package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;

import java.util.List;

public record ConfigStep(AbstractInstruction handler, ICondition condition, List<String> inputs, String output) {}
