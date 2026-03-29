package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

import java.util.List;

/**
 * Condition that evaluates to {@code true} if at least one
 * contained condition evaluates to {@code true}.
 *
 * <p>This implements a logical OR over one or more {@link ICondition} instances.</p>
 */
public final class OrCondition implements ICondition {
    private final ICondition[] conditions;

    public OrCondition(List<ICondition> conditions) {
        this.conditions = conditions.toArray(new ICondition[0]);
    }

    public OrCondition(ICondition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(IExecutionContext context) {
        boolean result = false;

        for (ICondition c : conditions)
            result |= c.test(context);

        return result;
    }
}
