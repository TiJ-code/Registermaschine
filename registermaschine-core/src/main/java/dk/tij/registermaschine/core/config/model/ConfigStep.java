package dk.tij.registermaschine.core.config.model;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;

import java.util.List;

public record ConfigStep(IStepHandler handler, ICondition condition, List<String> inputs, String output) {}
