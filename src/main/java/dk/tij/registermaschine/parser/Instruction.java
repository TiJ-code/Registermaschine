package dk.tij.registermaschine.parser;

public record Instruction(int opcode, int argument) {
    @Override
    public String toString() {
        return String.format("Instruction [opcode=%d, argument=%d]", opcode, argument);
    }
}
