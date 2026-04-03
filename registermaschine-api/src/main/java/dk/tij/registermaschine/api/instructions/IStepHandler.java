package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

public interface IStepHandler {
    void execute(IExecutionContext context, ICompiledOperand[] operands, int[] inputIndices, int outputIndex);

    default int requiredInputs() {
        return 1;
    }

    default boolean hasOutput() {
        return true;
    }

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
