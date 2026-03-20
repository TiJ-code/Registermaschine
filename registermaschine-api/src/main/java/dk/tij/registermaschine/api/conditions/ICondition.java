package dk.tij.registermaschine.api.conditions;

import dk.tij.registermaschine.api.runtime.IExecutionContext;

public interface ICondition {
    boolean test(IExecutionContext context);
}
