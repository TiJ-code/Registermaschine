package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;

public record ConcreteCompiledInstruction(byte opcode, int[] operands) implements ICompiledInstruction {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int operand : operands)
            sb.append(String.format("%02x", operand));
        return String.format("%02x%s", opcode, sb);
    }
}
