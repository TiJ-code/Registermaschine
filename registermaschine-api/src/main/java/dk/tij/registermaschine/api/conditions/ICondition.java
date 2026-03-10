package dk.tij.registermaschine.api.conditions;

import dk.tij.registermaschine.api.runtime.IExecutionContext;


/**
 * Represents a boolean condition that can be evaluated within
 * the context of the Registermaschine execution environment.
 *
 * <p>Conditions are used to determine whether certain instructions
 * should be executed, such as in conditional jumps or other
 * conditional operations.</p>
 *
 * <p>The evaluation is performed against an {@link IExecutionContext},
 * which provides access to registers, flags, and other runtime state.</p>
 *
 * <p>Implementation can define arbitrary logic using the context,
 * for example:</p>
 * <ul>
 *     <li>Checking processor flags (zero, negative, overflow)</li>
 *     <li>Comparing register values</li>
 *     <li>Evaluating complex expressions based on multiple registers</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICondition {
    /**
     * Evaluates this condition using the given execution context.
     *
     * @param context the runtime context providing registers, flags, and state
     * @return {@code true} if the condition is satisfied, {@code false} otherwise
     */
    boolean test(IExecutionContext context);
}
