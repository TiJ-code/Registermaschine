package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.ExecutionContext;

public final class EqualsZeroCondition implements Condition {
    @Override
    public boolean test(ExecutionContext context) {
        return context.getZeroFlag();
    }
}
