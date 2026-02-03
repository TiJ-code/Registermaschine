package dk.tij.registermaschine.core.config.internal.nodes;

import dk.tij.registermaschine.core.config.internal.ConditionNode;

public record NotNode(ConditionNode inner) implements ConditionNode {}
