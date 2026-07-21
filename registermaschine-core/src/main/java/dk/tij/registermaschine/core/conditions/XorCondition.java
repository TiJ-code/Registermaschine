package dk.tij.registermaschine.core.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

import java.util.List;

/**
 * Condition that evaluates to {@code true} only if an odd number
 * of contained conditions evaluate to {@code true}.
 *
 * <p>This implements a logical XOR over one or more {@link ICondition} instances.</p>
 *
 * @since 1.2.0
 * @author TiJ
 */
public class XorCondition implements ICondition {
    private static final ILogger log = LoggerFactory.getLogger(XorCondition.class);

    private final ICondition[] conditions;

    /**
     * Constructs an XorCondition from a list of conditions.
     *
     * @param conditions the list of conditions to combine
     */
    public XorCondition(List<ICondition> conditions) {
        this.conditions = conditions.toArray(new ICondition[0]);
    }

    /**
     * Constructs an XorCondition from a variable number of conditions.
     *
     * @param conditions the conditions to combine
     */
    public XorCondition(ICondition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(IExecutionContext context) {
        boolean result = true;

        for (ICondition c : conditions) {
            if (c.test(context)) {
                result = !result;
            }
        }

        log.trace("Evaluated condition to {}", result);

        return result;
    }
}
