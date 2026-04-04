package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

/**
 * Represents a single executable step within a compiled instruction.
 *
 * <p>A {@link IStepHandler} defines the runtime behaviour of a step in an
 * instruction chain. Each step operates on a set of operands and interacts
 * with the {@link IExecutionContext} to read and write register values.</p>
 *
 * <p>Step handlers are typically stateless and may be reused across multiple
 * instructions via a registry.</p>
 *
 * <p>Execution flow:</p>
 * <ul>
 *     <li>Inputs are resolved using {@code inputIndices}</li>
 *     <li>The step logic is executed via {@link #execute}</li>
 *     <li>If applicable, a result is written to {@code outputIndex}</li>
 * </ul>
 *
 * @since 2.0.0
 * @author TiJ
 */
public interface IStepHandler {
    /**
     * Executes this step using the given execution context and operands.
     *
     * @param context the current execution context providing access
     * @param operands all operands of the instruction
     * @param inputIndices indices of operands used as inputs for this step
     * @param outputIndex index of the operand used as output, or {@code -1} if no output is defined
     */
    void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex);

    /**
     * Resolves the integer value of a compiled operand.
     *
     * <p>If the operand is of type {@link OperandType#REGISTER}, the value is
     * retrieved from the execution context. Otherwise, the literal value is returned</p>
     *
     * @param context the execution context
     * @param operand the operand to resolve
     * @return the resolved integer value
     */
    default int getValueFromOperand(IExecutionContext context, ICompiledOperand operand) {
        if (operand.type().equals(OperandType.REGISTER))
            return context.getRegister(operand.value());
        return operand.value();
    }

    /**
     * Returns the minimum number of input operands required by this step.
     *
     * <p>Implementations should override this method if they require more.</p>
     *
     * @return the minimum number of required input operands (default: 1)
     */
    default int requiredInputs() {
        return 1;
    }

    /**
     * Indicates whether this step produces an output value.
     *
     * <p>If {@code true}, an output operand index must be provided during execution.</p>
     *
     * @return {@code true} if this step writes to an output operand; {@code false} otherwise
     */
    default boolean hasOutput() {
        return true;
    }

    /**
     * Validates the operands and indices before execution.
     *
     * <p>This method checks whether the required number of operands is present and
     * whether an output operand is defined if required.</p>
     *
     * <p>Implementations may override this method to add additional validation logic.</p>
     *
     * @param operands all operands of the instruction
     * @param inputIndices indices of operands used as inputs
     * @param outputIndex index of output operand, or {@code -1} if none
     * @throws IllegalArgumentException if validation fails
     */
    default void validate(ICompiledOperand[] operands, int[] inputIndices, int outputIndex) {
        if (operands.length < requiredInputs()) {
            throw new IllegalArgumentException(
                    "Step requires at least %d operands, got %d".formatted(requiredInputs(), operands.length)
            );
        }

        if (hasOutput() && outputIndex < 0) {
            throw new IllegalArgumentException("Step requires an output operand");
        }
    }
}
