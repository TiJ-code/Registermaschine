package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.ExecutionContext;

import java.util.List;

public final class AndCondition implements Condition {
    private final Condition[] conditions;

    public AndCondition(List<Condition> conditions) {
        this.conditions = conditions.toArray(new Condition[0]);
    }

    public AndCondition(Condition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(ExecutionContext context) {
        boolean result = true;

        for (Condition c : conditions)
            result &= c.test(context);

        return result;
    }
}
