package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.error.InvalidOperandException;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

import java.util.Objects;

public class ChainedInstruction {
    private final int operandCount;
    private final ICondition condition;
    private final ICompiledStep[] steps;

    public ChainedInstruction(int operandCount, ICondition condition, ICompiledStep[] steps) {
        this.operandCount = operandCount;
        this.condition = condition;
        this.steps = Objects.requireNonNull(steps, "steps must not be null");
    }

    public void execute(IExecutionContext context, ICompiledOperand[] operands) {
        if (!shouldExecute(context, condition))
            return;

        for (var step : steps) {
            IStepHandler handler = step.handler();
            handler.validate(operands, step.inputIndices(), step.outputIndex());

            if (shouldExecute(context, step.condition())) {
                handler.execute(context, operands, step.inputIndices(), step.outputIndex());
            }
        }
    }

    public void validateOperands(ICompiledOperand[] operands) {
        for (var step : steps) {
            try {
                step.handler().validate(operands, step.inputIndices(), step.outputIndex());
            } catch (IllegalArgumentException e) {
                throw new InvalidOperandException("Invalid operands for %s step.".formatted(step));
            }
        }

    }

    public int getOperandCount() {
        return operandCount;
    }

    public ICompiledStep[] getSteps() {
        return steps;
    }

    public ICondition getCondition() {
        return condition;
    }

    private static boolean shouldExecute(IExecutionContext context, ICondition condition) {
        return condition == null || condition.test(context);
    }
}
