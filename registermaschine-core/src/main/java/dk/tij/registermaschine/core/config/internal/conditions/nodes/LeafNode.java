package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A terminal node representing a specific class requirement.
 *
 * @param className The fully qualified name of the class to check for
 *
 * @since 1.0.0
 * @author TiJ
 */
public record LeafNode(String className) implements ConditionNode {}
