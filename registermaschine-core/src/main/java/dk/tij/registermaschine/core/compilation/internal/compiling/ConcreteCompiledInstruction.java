package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledInstruction;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;

/**
 * A standard implementation of a compiled instruction.
 *
 * <p>This record represents a single machine operation, consisting of a
 * unique operation code (opcode) and its associated parameters (operands).</p>
 *
 * @param opcode   The byte representation of the instruction to be executed
 * @param operands An array of {@link ICompiledOperand} required by this instruction.
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConcreteCompiledInstruction(int opcode, ICompiledOperand[] operands) implements ICompiledInstruction {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ICompiledOperand operand : operands)
            sb.append(String.format("%s", operand.toString()));
        return String.format("%02x%s", opcode, sb);
    }
}
