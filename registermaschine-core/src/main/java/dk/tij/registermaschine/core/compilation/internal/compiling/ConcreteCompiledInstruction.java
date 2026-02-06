package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;

public record ConcreteCompiledInstruction(byte opcode, ICompiledOperand[] operands) implements ICompiledInstruction {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ICompiledOperand operand : operands)
            sb.append(String.format("%s", operand.toString()));
        return String.format("%02x%s", opcode, sb);
    }
}
