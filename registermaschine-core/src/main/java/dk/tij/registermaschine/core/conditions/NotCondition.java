package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;

public final class NotCondition implements ICondition {
    private final ICondition inner;

    public NotCondition(ICondition condition) {
        this.inner = condition;
    }

    @Override
    public boolean test(ExecutionContext context) {
        return !inner.test(context);
    }
}
