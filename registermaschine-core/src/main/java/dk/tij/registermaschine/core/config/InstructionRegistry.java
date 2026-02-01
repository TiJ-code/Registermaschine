package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.AbstractInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionRegistry {
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
        return byOpcode.get(opcode);
    }

    public byte getOpcode(String name) {
        Byte op = byName.get(name.toLowerCase());
        if (op == null)
            throw new IllegalArgumentException("Unknown instruction: "+ name);
        return op;
    }

    public List<InstructionDescriptor> getInstructions() {
        return instructions;
    }
}
