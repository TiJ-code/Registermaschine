package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

import java.util.Objects;

/**
 * @since 2.0.0
 * @author TiJ
 */
public class ChainedInstruction implements IInstruction{
    private final int operandCount;
    private final ICondition condition;
    private final ICompiledStep[] steps;
    private final IOperandValidator validator;

    public ChainedInstruction(int operandCount, ICondition condition, ICompiledStep[] steps) {
        this(operandCount, condition, steps, IOperandValidator.defaultValidator());
    }

    public ChainedInstruction(int operandCount, ICondition condition,
                              ICompiledStep[] steps, IOperandValidator validator) {
        this.operandCount = operandCount;
        this.condition = condition;
        this.steps = Objects.requireNonNull(steps, "steps cannot be null");
        this.validator = Objects.requireNonNull(validator, "validator cannot be null");
    }

    @Override
    public void execute(IExecutionContext context, ICompiledOperand[] operands) {
        validator.validate(operands, operandCount);

        if (!shouldExecute(context, condition))
            return;

        for (ICompiledStep step : steps) {
            IStepHandler handler = step.handler();
            handler.validate(operands, step.inputIndices(), step.outputIndex());

            if (shouldExecute(context, step.condition())) {
                handler.execute(context, operands, step.inputIndices(), step.outputIndex());
            }
        }
    }

    public int operandCount() {
        return operandCount;
    }

    public ICompiledStep[] steps() {
        return steps;
    }
    
    public ICondition condition() {
        return condition;
    }

    private boolean shouldExecute(IExecutionContext context, ICondition condition) {
        return condition == null || condition.test(context);
    }
}
