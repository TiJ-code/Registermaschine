package dk.tij.registermaschine.core.conditions.api;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public interface ICondition {
    boolean test(IExecutionContext context);
}
