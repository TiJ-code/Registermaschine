package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.config.ConfigStep;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

import java.util.List;

/**
 * @since 2.0.0
 * @author TiJ
 */
public abstract class AbstractChainedInstruction extends AbstractInstruction {
    private final List<ConfigStep> configSteps;

    public AbstractChainedInstruction(byte opcode, int operandCount, ICondition condition, List<ConfigStep> configSteps) {
        super(opcode, operandCount, condition);
        this.configSteps = configSteps;
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        for (ConfigStep step : configSteps) {
            AbstractInstruction handler = step.handler();
            if (handler.shouldExecute(context)) {
                handler.executeInstruction(context, operands);
            }
        }
    }
}
