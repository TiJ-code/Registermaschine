package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;

public final class LessThanZeroCondition implements ICondition {
    @Override
    public boolean test(ExecutionContext context) {
        return !context.getZeroFlag() && context.getNegativeFlag();
    }
}
