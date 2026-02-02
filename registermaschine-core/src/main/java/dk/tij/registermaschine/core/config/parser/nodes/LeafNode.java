package dk.tij.registermaschine.core.config.parser.nodes;

import dk.tij.registermaschine.core.config.parser.ConditionNode;

public record LeafNode(String className) implements ConditionNode {}
