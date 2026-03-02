package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public final class LessThanZeroCondition implements ICondition {
    @Override
    public boolean test(IExecutionContext context) {
        return !context.getZeroFlag() && context.getNegativeFlag();
    }
}
