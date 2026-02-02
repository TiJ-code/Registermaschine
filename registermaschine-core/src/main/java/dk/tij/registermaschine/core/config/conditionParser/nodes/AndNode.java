package dk.tij.registermaschine.core.config.conditionParser.nodes;

import dk.tij.registermaschine.core.config.conditionParser.ConditionNode;

public record AndNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
