package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

/**
 * Condition that evaluates to the logical negation of an inner condition.
 *
 * <p>This implements a logical NOT operation on a single {@link ICondition}.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class NotCondition implements ICondition {
    private final ICondition inner;

    /**
     * Constructs a NotCondition wrapping another condition.
     *
     * @param condition the condition to negate
     */
    public NotCondition(ICondition condition) {
        this.inner = condition;
    }

    @Override
    public boolean test(IExecutionContext context) {
        return !inner.test(context);
    }
}
