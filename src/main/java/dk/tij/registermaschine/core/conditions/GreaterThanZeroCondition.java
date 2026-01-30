package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.ExecutionContext;

public final class GreaterThanZeroCondition implements Condition {
    @Override
    public boolean test(ExecutionContext context) {
        return !context.getZeroFlag() && !context.getNegativeFlag();
    }
}
