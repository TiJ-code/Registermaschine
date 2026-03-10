package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.config.ConfigInstruction;
import dk.tij.registermaschine.api.error.ExistingInstructionException;
import dk.tij.registermaschine.api.error.UnknownInstructionException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.*;

/**
 * Concrete implementations of {@link IInstructionSet}.
 *
 * <p>This class maintains a registry of all available instructions for the Registermaschine.
 * It supports lookup by mnemonic or opcode, enforces uniqueness of opcode, and allows
 * removing instructions by their handler class.</p>
 *
 * <p>Internally, the instructions are stored in:</p>
 * <ul>
 *     <li>{@code instructions} - ordered list of all instructions</li>
 *     <li>{@code byName} - map from lowercase mnemonic to instruction</li>
 *     <li>{@code ByOpcode} - map from opcode to instruction</li>
 * </ul>
 *
 * <p>This class throws {@link ExistingInstructionException} if an instruction
 * with the same opcode is already registered, and {@link UnknownInstructionException}
 * if lookup fails.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConcreteInstructionSet implements IInstructionSet {
    /**
     * Ordered list of registered instructions
     */
    private final List<ConfigInstruction> instructions = new ArrayList<>();
    /**
     * Map from lowercase mnemonic to configuration
     */
    private final Map<String, ConfigInstruction> byName = new HashMap<>();
    /**
     * Map from opcode to configuration
     */
    private final Map<Byte, ConfigInstruction> byOpcode = new HashMap<>();

    /**
     * Registers a new instruction in the set.
     *
     * <p>The opcode must be unique; if it is already registered an
     * {@link ExistingInstructionException} is thrown.</p>
     *
     * @param configInstruction the instruction configuration to register
     */
    @Override
    public void registerInstruction(ConfigInstruction configInstruction) {
        byte opcode = configInstruction.opcode();

        if (byOpcode.containsKey(opcode))
            throw new ExistingInstructionException("Opcode " + opcode + " is already registered!");

        instructions.add(configInstruction);
        byName.put(configInstruction.mnemonic().toLowerCase(), configInstruction);
        byOpcode.put(opcode, configInstruction);
    }

    /**
     * Removes all instructions implemented by a specific handler class
     * from the set
     *
     * <p>Any matching instruction is removed from the list, mnemonic map,
     * and opcode map.</p>
     *
     * @param instruction the {@link AbstractInstruction} class to prohibit
     */
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

    /**
     * Returns an immutable copy of all registered instructions.
     *
     * @return list of {@link ConfigInstruction} entries
     */
    @Override
    public List<ConfigInstruction> getInstructions() {
        return List.copyOf(instructions);
    }
}
