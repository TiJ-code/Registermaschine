package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

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
