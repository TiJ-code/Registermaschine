package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

import java.util.List;

/**
 * Condition that evaluates to {@code true} only if all
 * contained conditions evaluate to {@code true}.
 *
 * <p>This implements a logical AND over one or more {@link ICondition} instances.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class AndCondition implements ICondition {
    private final ICondition[] conditions;

    /**
     * Constructs an AndCondition from a list of conditions.
     *
     * @param conditions the list of conditions to combine
     */
    public AndCondition(List<ICondition> conditions) {
        this.conditions = conditions.toArray(new ICondition[0]);
    }

    /**
     * Constructs an AndCondition from a variable number of conditions.
     * @param conditions the conditions to combine
     */
    public AndCondition(ICondition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(IExecutionContext context) {
        boolean result = true;

        for (ICondition c : conditions)
            result &= c.test(context);

        return result;
    }
}
