package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.exception.ExistingInstructionException;
import dk.tij.registermaschine.core.exception.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionSet {
    private final List<InstructionInfo> instructions = new ArrayList<>();
    private final Map<String, InstructionInfo> byName = new HashMap<>();
    private final Map<Byte, InstructionInfo> byOpcode = new HashMap<>();

    public void registerInstruction(String mnemonic, byte opcode, String description, AbstractInstruction handler) {
        if (byOpcode.containsKey(opcode))
            throw new ExistingInstructionException("Opcode " + opcode + " is already registered!");

        InstructionInfo entry = new InstructionInfo(mnemonic, description, opcode, handler);
        instructions.add(entry);
        byName.put(mnemonic.toLowerCase(), entry);
        byOpcode.put(opcode, entry);
    }

    public AbstractInstruction getHandler(byte opcode) {
        InstructionInfo entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.handler();
    }

    public byte getOpcode(String mnemonic) {
        InstructionInfo entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        return entry.opcode();
    }

    public List<InstructionInfo> getInstructions() {
        return instructions;
    }
}
