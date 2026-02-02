package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.runtime.ExecutionContext;

public interface ICondition {
    boolean test(ExecutionContext context);
}
