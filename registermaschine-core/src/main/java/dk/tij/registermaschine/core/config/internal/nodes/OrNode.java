package dk.tij.registermaschine.core.config.internal.nodes;

import dk.tij.registermaschine.core.config.internal.ConditionNode;

public record OrNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
