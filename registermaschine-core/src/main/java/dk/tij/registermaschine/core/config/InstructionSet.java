package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.error.ExistingInstructionException;
import dk.tij.registermaschine.core.error.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.*;

public final class InstructionSet implements IInstructionSet {
    private final List<InstructionInfo> instructions = new ArrayList<>();
    private final Map<String, InstructionInfo> byName = new HashMap<>();
    private final Map<Byte, InstructionInfo> byOpcode = new HashMap<>();

    @Override
    public void registerInstruction(String mnemonic, String description, byte opcode, AbstractInstruction handler) {
        if (byOpcode.containsKey(opcode))
            throw new ExistingInstructionException("Opcode " + opcode + " is already registered!");

        InstructionInfo entry = new InstructionInfo(mnemonic, description, opcode, handler);
        instructions.add(entry);
        byName.put(mnemonic.toLowerCase(), entry);
        byOpcode.put(opcode, entry);
    }

    @Override
    public void prohibitInstructionHandler(Class<? extends AbstractInstruction> instruction) {
        List<InstructionInfo> permittedInstructions = instructions.stream()
                    .filter(Objects::nonNull)
                    .filter(i -> Objects.nonNull(i.handler()))
                    .filter(i -> i.handler().getClass().equals(instruction))
                    .toList();
        for (InstructionInfo instr : permittedInstructions) {
            byName.remove(instr.mnemonic().toLowerCase(), instr);
            byOpcode.remove(instr.opcode(), instr);
            instructions.remove(instr);
        }
    }

    @Override
    public AbstractInstruction getHandler(String mnemonic) {
        InstructionInfo entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        return entry.handler();
    }

    @Override
    public AbstractInstruction getHandler(byte opcode) {
        InstructionInfo entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.handler();
    }

    @Override
    public String getMnemonic(byte opcode) {
        InstructionInfo entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.mnemonic();
    }

    @Override
    public byte getOpcode(String mnemonic) {
        InstructionInfo entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        return entry.opcode();
    }

    @Override
    public boolean contains(String mnemonic) {
        return instructions.stream().filter(Objects::nonNull).anyMatch(i -> i.mnemonic().equals(mnemonic));
    }

    @Override
    public boolean contains(byte mnemonic) {
        return instructions.stream().filter(Objects::nonNull).anyMatch(i -> i.opcode() == mnemonic);
    }

    @Override
    public List<InstructionInfo> getInstructions() {
        return instructions;
    }
}
