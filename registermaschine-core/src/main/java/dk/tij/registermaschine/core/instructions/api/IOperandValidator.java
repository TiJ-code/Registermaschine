package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;

@FunctionalInterface
public interface IOperandValidator {
    void validate(ICompiledOperand[] operands, int expectedCount);

    static IOperandValidator defaultValidator() {
        return ((operands, expectedCount) -> {
           if (operands.length != expectedCount) {
               throw new IllegalArgumentException("Invalid operand count: %d expected %d"
                       .formatted(operands.length, expectedCount));
           }
        });
    }

    static IOperandValidator noValidator() {
        return ((operands, expectedCount) -> {});
    }
}
