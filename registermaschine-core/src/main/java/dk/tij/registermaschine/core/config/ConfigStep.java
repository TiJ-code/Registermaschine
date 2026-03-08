package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;

import java.util.List;

public record ConfigStep(AbstractInstruction handler, List<String> inputs, String output) {}
