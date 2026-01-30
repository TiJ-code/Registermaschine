package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.AbstractInstruction;

import java.util.HashMap;
import java.util.Map;

public class InstructionRegistry {
    private final Map<Byte, AbstractInstruction> opcodeMap = new HashMap<>();

    public void registerInstruction(byte opcode, AbstractInstruction handler) {
        if (opcodeMap.containsKey(opcode))
            throw new IllegalArgumentException("Opcode " + opcode + " is already registered!");
        opcodeMap.put(opcode, handler);
    }

    public AbstractInstruction getHandler(byte opcode) {
        return opcodeMap.get(opcode);
    }

    public Map<Byte, AbstractInstruction> Map() {
        return opcodeMap;
    }
}
