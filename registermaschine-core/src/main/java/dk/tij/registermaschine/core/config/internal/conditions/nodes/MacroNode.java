package dk.tij.registermaschine.core.config.internal.conditions.nodes;

import dk.tij.registermaschine.core.config.internal.conditions.ConditionNode;

/**
 * A terminal node representing a reusable macro reference.
 *
 * @param macroName The identifier of the macro to be expanded or evaluated.
 *
 * @since 1.0.0
 * @author TiJ
 */
public record MacroNode(String macroName) implements ConditionNode {
}
