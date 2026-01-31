package dk.tij.registermaschine.core.instructions;

public record CompiledInstruction(byte opcode, int[] operands) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int operand : operands)
            sb.append(String.format("%02x", operand));
        return String.format("%02x%s", opcode, sb);
    }
}
