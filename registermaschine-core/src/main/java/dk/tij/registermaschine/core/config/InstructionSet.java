package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.exception.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionSet {
    private final List<InstructionDescriptor> instructions = new ArrayList<>();
    private final Map<String, Byte> byName = new HashMap<>();
    private final Map<Byte, AbstractInstruction> byOpcode = new HashMap<>();

    public void registerInstruction(String name, byte opcode, InstructionDescriptor descriptor, AbstractInstruction handler) {
        if (byOpcode.containsKey(opcode))
            throw new IllegalArgumentException("Opcode " + opcode + " is already registered!");
        instructions.add(descriptor);
        byName.put(name.toLowerCase(), opcode);
        byOpcode.put(opcode, handler);
    }

    public AbstractInstruction getHandler(byte opcode) {
        AbstractInstruction instruction = byOpcode.get(opcode);
        if (instruction == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return instruction;
    }

    public byte getOpcode(String name) {
        Byte op = byName.get(name.toLowerCase());
        if (op == null)
            throw new UnknownInstructionException("No instruction found with name " + name);
        return op;
    }

    public List<InstructionDescriptor> getInstructions() {
        return instructions;
    }
}
