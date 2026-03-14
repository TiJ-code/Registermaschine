package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

/**
 * @since 2.0.0
 * @author TiJ
 */
public class ChainedInstruction {
    private final int operandCount;
    private final ICondition condition;
    private final ICompiledStep[] steps;

    public ChainedInstruction(int operandCount, ICondition condition, ICompiledStep[] steps) {
        this.operandCount = operandCount;
        this.condition = condition;
        this.steps = steps;
    }

    public void execute(IExecutionContext context, ICompiledOperand[] operands) {
        if (!validOperands(operands)) {
            throw new RuntimeException("Insufficient operands: %d expected %d"
                    .formatted(operands.length, operandCount));
        }

        if (!shouldExecute(context, condition))
            return;

        for (ICompiledStep step : steps) {
            if (shouldExecute(context, step.condition())) {
                step.handler().execute(context, operands, step.inputIndices(), step.outputIndex());
            }
        }
    }
    
    public ICompiledStep[] steps() {
        return steps;
    }
    
    public ICondition condition() {
        return condition;
    }

    private boolean validOperands(ICompiledOperand[] operands) {
        return operands.length == operandCount;
    }

    private boolean shouldExecute(IExecutionContext context, ICondition condition) {
        if (condition == null)
            return true;
        return condition.test(context);
    }
}
