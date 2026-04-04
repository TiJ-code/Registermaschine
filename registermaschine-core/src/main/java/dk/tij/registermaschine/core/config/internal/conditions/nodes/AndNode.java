package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A logical intersection node. Evaluates to true only if both sides are true.
 *
 * @param left  The left-hand side of the AND expression
 * @param right The right-hand side of the AND expression
 *
 * @since 1.0.0
 * @author TiJ
 */
public record AndNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
