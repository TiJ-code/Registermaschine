package dk.tij.registermaschine.core.config.parser.nodes;

import dk.tij.registermaschine.core.config.parser.ConditionNode;

public record OrNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
