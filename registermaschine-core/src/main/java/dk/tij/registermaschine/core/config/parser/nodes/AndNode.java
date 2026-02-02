package dk.tij.registermaschine.core.config.parser.nodes;

import dk.tij.registermaschine.core.config.parser.ConditionNode;

public record AndNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
