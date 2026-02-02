package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;

import java.util.List;

public final class AndCondition implements ICondition {
    private final ICondition[] conditions;

    public AndCondition(List<ICondition> conditions) {
        this.conditions = conditions.toArray(new ICondition[0]);
    }

    public AndCondition(ICondition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(ExecutionContext context) {
        boolean result = true;

        for (ICondition c : conditions)
            result &= c.test(context);

        return result;
    }
}
