package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A logical decorator that negates the result of its child node.
 *
 * @param inner The inner condition to be inverted
 *
 * @since 1.0.0
 * @author TiJ
 */
public record NotNode(ConditionNode inner) implements ConditionNode {}
