package dk.tij.registermaschine.parser;

public class Instruction {
    private final int opcode;
    private final int argument;

    public Instruction(int opcode, int argument) {
        this.opcode = opcode;
        this.argument = argument;
    }

    public int opcode() {
        return opcode;
    }

    public int argument() {
        return argument;
    }

    @Override
    public String toString() {
        return String.format("Instruction [opcode=%d, argument=%d]", opcode, argument);
    }
}
