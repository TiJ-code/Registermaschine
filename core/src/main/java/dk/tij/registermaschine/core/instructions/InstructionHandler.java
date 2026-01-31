package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.ExecutionContext;

public interface InstructionHandler {
    void executeInstruction(ExecutionContext context, int[] operands);
}
