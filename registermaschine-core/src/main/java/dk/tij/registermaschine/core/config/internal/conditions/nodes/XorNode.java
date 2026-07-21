package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A logical intersection node. Evaluates to {@code true} if either one
 * side evaluates to {@code true}, but not both.
 *
 * @param left  The left-hand side of the XOR expression
 * @param right The right-hand side of the XOR expression
 *
 * @since 1.2.0
 * @author TiJ
 */
public record XorNode(ConditionNode left, ConditionNode right) implements ConditionNode {}
