package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

/**
 * Condition that evaluates to {@code true} if the {@link IExecutionContext}'s
 * negative flag is set and the zero flag is not set.
 *
 * <p>This is typically used after arithmetic instructions
 * to check whether the last operation produced a negative
 * non-zero result.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class LessThanZeroCondition implements ICondition {
    @Override
    public boolean test(IExecutionContext context) {
        return !context.getZeroFlag() && context.getNegativeFlag();
    }
}
