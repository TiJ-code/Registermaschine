package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.AbstractInstruction;

import java.util.HashMap;
import java.util.Map;

public class InstructionRegistry {
    private final Map<String, Byte> byName = new HashMap<>();
    private final Map<Byte, AbstractInstruction> byOpcode = new HashMap<>();

    public void registerInstruction(String name, byte opcode, AbstractInstruction handler) {
        if (byOpcode.containsKey(opcode))
            throw new IllegalArgumentException("Opcode " + opcode + " is already registered!");
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
}
