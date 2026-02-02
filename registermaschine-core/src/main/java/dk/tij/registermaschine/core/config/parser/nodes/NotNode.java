package dk.tij.registermaschine.core.config.parser.nodes;

import dk.tij.registermaschine.core.config.parser.ConditionNode;

public record NotNode(ConditionNode inner) implements ConditionNode {}
