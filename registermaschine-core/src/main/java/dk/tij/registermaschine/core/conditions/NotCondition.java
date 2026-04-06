package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Condition that evaluates to the logical negation of an inner condition.
 *
 * <p>This implements a logical NOT operation on a single {@link ICondition}.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class NotCondition implements ICondition {
    private static final Logger log = LoggerFactory.getLogger(NotCondition.class);

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
        boolean result = !inner.test(context);

        log.trace("Evaluated condition to {}", result);

        return result;
    }
}
