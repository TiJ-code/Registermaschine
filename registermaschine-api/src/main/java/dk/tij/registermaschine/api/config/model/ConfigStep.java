package dk.tij.registermaschine.api.config.model;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.IStepHandler;

import java.util.List;

public record ConfigStep(IStepHandler handler, ICondition condition, List<String> inputs, String output) {}
