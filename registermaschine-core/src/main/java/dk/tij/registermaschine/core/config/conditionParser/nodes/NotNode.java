package dk.tij.registermaschine.core.config.conditionParser.nodes;

import dk.tij.registermaschine.core.config.conditionParser.ConditionNode;

public record NotNode(ConditionNode inner) implements ConditionNode {}
