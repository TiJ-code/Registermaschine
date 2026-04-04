package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.error.InvalidOperandException;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

import java.util.Objects;

/**
 * Represents a fully compiled instruction consisting of a sequence of executable steps.
 *
 * <p>A {@link ChainedInstruction} is the final result of the precompilation stage.
 * It encapusaltes:</p>
 * <ul>
 *     <li>A fixed number of operands</li>
 *     <li>An optional instruction-level {@link ICondition}</li>
 *     <li>An ordered array of {@link ICompiledStep} instances</li>
 * </ul>
 *
 * <p>Execution is performed step-by-step. Each step may have its own condition,
 * allowing fine-grained control over execution flow within the instruction.</p>
 *
 * <p>Execution flow:</p>
 * <ol>
 *     <li>Check the instruction-level condition</li>
 *     <li>If true, iterate through all steps</li>
 *     <li>Validate operands for each step</li>
 *     <li>Check step-level condition</li>
 *     <li>Execute the step if its condition is satisfied</li>
 * </ol>
 *
 * <p>This design enables flexible and composable instruction definitions,
 * including conditional execution and multi-step operations.</p>
 *
 * @version 2.0.0
 * @author TiJ
 */
public class ChainedInstruction {
    /**
     * The number of operands expected by this instruction.
     */
    private final int operandCount;

    /**
     * Optional condition that determines whether the instruction should execute.
     */
    private final ICondition condition;

    /**
     * Ordered list of compiled steps that define the instruction's behaviour.
     */
    private final ICompiledStep[] steps;

    /**
     * Creates a new chained instruction
     *
     * @param operandCount the number of operands this instruction expects
     * @param condition    the instruction-level condition (may be {@code null})
     * @param steps        the sequence of compiled steps (must not be {@code null}
     */
    public ChainedInstruction(int operandCount, ICondition condition, ICompiledStep[] steps) {
        this.operandCount = operandCount;
        this.condition = condition;
        this.steps = Objects.requireNonNull(steps, "steps must not be null");
    }

    /**
     * Executes this instruction in the given execution context.
     *
     * <p>If the instruction-level condition is not satisfied, execution is skipped.</p>
     * <p>Each step is validated and executed sequentially. Step-level conditions
     * are evaluated before execution.</p>
     *
     * @param context  the execution context providing register access
     * @param operands the operands associated with this instruction
     */
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

    /**
     * Validates the given operands against all steps in this instruction.
     *
     * <p>This method ensures that each step's handler accepts the provided
     * operands and indices. It does not execute any logic.</p>
     *
     * @param operands the operands to validate
     * @throws InvalidOperandException if any step validation fails
     */
    public void validateOperands(ICompiledOperand[] operands) {
        for (var step : steps) {
            try {
                step.handler().validate(operands, step.inputIndices(), step.outputIndex());
            } catch (IllegalArgumentException e) {
                throw new InvalidOperandException("Invalid operands for %s step.".formatted(step));
            }
        }

    }

    /**
     * Returns the number of operands required by this instruction.
     *
     * @return the operand count
     */
    public int getOperandCount() {
        return operandCount;
    }

    /**
     * Returns the compiled steps of this instruction.
     *
     * @return the array of {@link ICompiledStep}
     */
    public ICompiledStep[] getSteps() {
        return steps;
    }

    /**
     * Returns the instruction-level condition.
     *
     * @return the {@link ICondition}, or {@code null} if none is defined
     */
    public ICondition getCondition() {
        return condition;
    }

    /**
     * Determines whether execution should proceed based on the given condition
     *
     * @param context the execution context
     * @param condition the condition to evaluate (may be {@code null})
     * @return {@code true} if execution should proceed, otherwise {@code false}
     */
    private static boolean shouldExecute(IExecutionContext context, ICondition condition) {
        return condition == null || condition.test(context);
    }
}
