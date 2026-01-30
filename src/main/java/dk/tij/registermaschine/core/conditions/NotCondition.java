package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.ExecutionContext;

public final class NotCondition implements Condition {
    private final Condition inner;

    public NotCondition(Condition condition) {
        this.inner = condition;
    }

    @Override
    public boolean test(ExecutionContext context) {
        return !inner.test(context);
    }
}
