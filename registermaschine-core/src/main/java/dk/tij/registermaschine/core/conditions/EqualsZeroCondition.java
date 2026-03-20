package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public final class EqualsZeroCondition implements ICondition {
    @Override
    public boolean test(IExecutionContext context) {
        return context.getZeroFlag();
    }
}
