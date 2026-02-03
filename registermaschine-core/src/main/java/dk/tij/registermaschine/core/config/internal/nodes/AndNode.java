package dk.tij.registermaschine.core.config.internal.nodes;

import dk.tij.registermaschine.core.config.internal.ConditionNode;

public record AndNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
