package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

/**
 * Condition that evaluates to {@code true} if the {@link IExecutionContext}'s
 * zero flag is set.
 *
 * <p>This is typically used after arithmetic instructions
 * to check whether the last operation produced a result.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class EqualsZeroCondition implements ICondition {
    @Override
    public boolean test(IExecutionContext context) {
        return context.getZeroFlag();
    }
}
