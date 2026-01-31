package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.ExecutionContext;

public interface Condition {
    boolean test(ExecutionContext context);
}
