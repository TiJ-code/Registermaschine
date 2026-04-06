package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.config.ConfigInstruction;
import dk.tij.registermaschine.api.error.ExistingInstructionException;
import dk.tij.registermaschine.api.error.UnknownInstructionException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.instructions.IInstructionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcreteInstructionSet.class);

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
    private final Map<Integer, ConfigInstruction> byOpcode = new HashMap<>();

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
        int opcode = configInstruction.opcode();

        if (byOpcode.containsKey(opcode)) {
            LOGGER.warn("Attempted to register duplicate opcode {} ({})", opcode, configInstruction);
            throw new ExistingInstructionException("Opcode " + opcode + " is already registered!");
        }

        instructions.add(configInstruction);
        byName.put(configInstruction.mnemonic().toLowerCase(), configInstruction);
        byOpcode.put(opcode, configInstruction);

        LOGGER.info("Registered instruction '{}' with opcode {}", configInstruction.mnemonic(), opcode);
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
        LOGGER.info("Prohibiting instructions handled by {}", instruction.getName());

        List<ConfigInstruction> permittedInstructions = instructions.stream()
                    .filter(Objects::nonNull)
                    .filter(i -> Objects.nonNull(i.handler()))
                    .filter(i -> i.handler().getClass().equals(instruction))
                    .toList();
        for (ConfigInstruction instr : permittedInstructions) {
            byName.remove(instr.mnemonic().toLowerCase(), instr);
            byOpcode.remove(instr.opcode(), instr);
            instructions.remove(instr);
            LOGGER.info("Removed instruction '{}' with opcode {}", instr.mnemonic(), instr.opcode());
        }
    }

    @Override
    public AbstractInstruction getHandler(String mnemonic) {
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null) {
            LOGGER.debug("Instruction with mnemonic {} not found", mnemonic);
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        }
        LOGGER.debug("Retrieved handler for mnemonic '{}'", mnemonic);
        return entry.handler();
    }

    @Override
    public AbstractInstruction getHandler(int opcode) {
        LOGGER.debug("Getting instruction handler by opcode {}", opcode);

        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null) {
            LOGGER.debug("Instruction with opcode {} not found", opcode);
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        }
        LOGGER.debug("Retrieved handler for opcode {}", opcode);
        return entry.handler();
    }

    @Override
    public String getMnemonic(int opcode) {
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null) {
            LOGGER.debug("Mnemonic lookup failed for opcode {}", opcode);
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        }
        return entry.mnemonic();
    }

    @Override
    public int getOpcode(String mnemonic) {
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null) {
            LOGGER.debug("Opcode lookup failed for mnemonic '{}'", mnemonic);
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        }
        return entry.opcode();
    }

    @Override
    public boolean contains(String mnemonic) {
        boolean result = instructions.stream().filter(Objects::nonNull).anyMatch(i -> i.mnemonic().equals(mnemonic));
        LOGGER.debug("Instruction set contains mnemonic '{}': {}", mnemonic, result);
        return result;
    }

    @Override
    public boolean contains(int opcode) {
        boolean result = instructions.stream().filter(Objects::nonNull).anyMatch(i -> i.opcode() == mnemonic);
        LOGGER.debug("Instruction set contains opcode '{}': {}", opcode, result);
        return result;
    }

    /**
     * Returns an immutable copy of all registered instructions.
     *
     * @return list of {@link ConfigInstruction} entries
     */
    @Override
    public List<ConfigInstruction> getInstructions() {
        LOGGER.trace("Returning {} configured instructions", instructions.size());
        return List.copyOf(instructions);
    }
}
