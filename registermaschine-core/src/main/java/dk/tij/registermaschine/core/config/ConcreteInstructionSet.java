package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.error.ExistingInstructionException;
import dk.tij.registermaschine.core.error.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.*;

public final class ConcreteInstructionSet implements IInstructionSet {
    private final List<ConfigInstruction> instructions = new ArrayList<>();
    private final Map<String, ConfigInstruction> byName = new HashMap<>();
    private final Map<Byte, ConfigInstruction> byOpcode = new HashMap<>();

    @Override
    public void registerInstruction(ConfigInstruction configInstruction) {
        byte opcode = configInstruction.opcode();

        if (byOpcode.containsKey(opcode))
            throw new ExistingInstructionException("Opcode " + opcode + " is already registered!");

        instructions.add(configInstruction);
        byName.put(configInstruction.mnemonic().toLowerCase(), configInstruction);
        byOpcode.put(opcode, configInstruction);
    }

    @Override
    public void prohibitInstructionHandler(Class<? extends AbstractInstruction> instruction) {
        List<ConfigInstruction> permittedInstructions = instructions.stream()
                    .filter(Objects::nonNull)
                    .filter(i -> Objects.nonNull(i.handler()))
                    .filter(i -> i.handler().getClass().equals(instruction))
                    .toList();
        for (ConfigInstruction instr : permittedInstructions) {
            byName.remove(instr.mnemonic().toLowerCase(), instr);
            byOpcode.remove(instr.opcode(), instr);
            instructions.remove(instr);
        }
    }

    @Override
    public AbstractInstruction getHandler(String mnemonic) {
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        return entry.handler();
    }

    @Override
    public AbstractInstruction getHandler(byte opcode) {
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.handler();
    }

    @Override
    public String getMnemonic(byte opcode) {
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.mnemonic();
    }

    @Override
    public byte getOpcode(String mnemonic) {
        ConfigInstruction entry = byName.get(mnemonic);
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
    public List<ConfigInstruction> getInstructions() {
        return List.copyOf(instructions);
    }
}
