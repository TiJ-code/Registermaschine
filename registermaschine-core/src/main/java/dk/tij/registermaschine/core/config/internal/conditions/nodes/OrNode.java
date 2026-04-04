package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A logical intersection node. Evaluates to true if at least one side is true.
 *
 * @param left  The left-hand side of the OR expression
 * @param right The right-hand side of the OR expression
 *
 * @since 1.0.0
 * @author TiJ
 */
public record OrNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
