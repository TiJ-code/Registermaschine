package dk.tij.registermaschine.core.conditions.internal;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

import java.util.List;

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
