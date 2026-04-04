package dk.tij.registermaschine.core.config.internal.conditions;

/**
 * The base interface for all nodes in a condition expression tree.
 *
 * <p>Implementing classes represent logical operations (AND, OR, NOT)
 * or terminal values (Leaf, Macro) used to evaluate configuration logic.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ConditionNode {}

