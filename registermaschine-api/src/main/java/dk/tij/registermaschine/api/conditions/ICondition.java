package dk.tij.registermaschine.api.conditions;

import dk.tij.registermaschine.api.runtime.IExecutionContext;


/**
 * Represents a condition that can be evaluated against an
 * {@link IExecutionContext}.
 *
 * <p>Conditions are typically used to control execution flow,
 * such as enabling or skipping instructions based on runtime state.</p>
 *
 * <p>The evaluation logic is implementation-defined and may depend
 * on any aspect of the provided execution context.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICondition {
    /**
     * Evaluates this condition using the given execution context.
     *
     * @param context the execution context providing access to runtime state
     * @return {@code true} if the condition is satisfied, {@code false} otherwise
     */
    boolean test(IExecutionContext context);
}
