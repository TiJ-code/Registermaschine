package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

public record NotNode(ConditionNode inner) implements ConditionNode {}
