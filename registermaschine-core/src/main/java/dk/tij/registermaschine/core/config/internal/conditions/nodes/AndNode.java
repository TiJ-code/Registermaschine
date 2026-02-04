package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

public record AndNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
