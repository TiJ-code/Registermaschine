package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.config.ConfigInstruction;

import java.util.List;

/**
 * Defines a registry of available instructions.
 *
 * <p>An {@link IInstructionSet} maintains mappings between instruction
 * mnemonics, opcodes, and their corresponding handlers.</p>
 *
 * <p>The instruction set is used during compilation and execution to
 * resolve instructions by mnemonic or opcode.</p>
 *
 * <p>Instructions are typically registered using {@link ConfigInstruction}
 * definitions, which describe their metadata and behaviour.</p>
 *
 * <p>Implementations may restrict or customise the available instructions,
 * for example by prohibiting specific handlers.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IInstructionSet {
    /**
     * Registers an instruction in the instruction set.
     *
     * @param configInstruction the instruction configuration
     */
    void registerInstruction(ConfigInstruction configInstruction);

    /**
     * Prohibits the use of a specific instruction handler.
     *
     * <p>If a prohibited handler is encountered during registration,
     * the implementation may reject or ignore the instruction.</p>
     *
     * @param instructionHandler the handler class to prohibit
     */
    void prohibitInstructionHandler(Class<? extends AbstractInstruction> instructionHandler);

    /**
     * Returns the instruction handler associated with the given mnemonic.
     *
     * @param mnemonic the instruction mnemonic
     * @return the corresponding instruction handler
     */
    AbstractInstruction getHandler(String mnemonic);

    /**
     * Returns the instruction handler associated with the given opcode.
     *
     * @param opcode the instruction opcode
     * @return the corresponding instruction handler
     */
    AbstractInstruction getHandler(int opcode);

    /**
     * Returns the opcode associated with a mnemonic.
     *
     * @param mnemonic the instruction mnemonic
     * @return the opcode
     */
    int getOpcode(String mnemonic);

    /**
     * Returns the mnemonic associated with an opcode.
     *
     * @param opcode the instruction opcode
     * @return the instruction mnemonic
     */
    String getMnemonic(int opcode);

    /**
     * Checks whether an instruction with the given mnemonic exists.
     *
     * @param mnemonic the instruction mnemonic
     * @return {@code true} if present
     */
    boolean contains(String mnemonic);

    /**
     * Checks whether an instruction with the given opcode exists.
     *
     * @param opcode the instruction opcode
     * @return {@code true} if present
     */
    boolean contains(int opcode);

    /**
     * Returns all registered instruction configurations.
     *
     * <p>The returned list reflects the current instruction definitions
     * known to the instruction set.</p>
     *
     * @return a list of instruction configurations
     */
    List<ConfigInstruction> getInstructions();
}
