package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class NotCondition implements ICondition {
    private final ICondition inner;

    public NotCondition(ICondition condition) {
        this.inner = condition;
    }

    @Override
    public boolean test(IExecutionContext context) {
        return !inner.test(context);
    }
}
