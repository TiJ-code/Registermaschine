package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.config.ConfigInstruction;

import java.util.List;

/**
 * Represents the instruction registry used by the Registermaschine runtime
 *
 * <p>An {@link IInstructionSet} maintains the mapping between instruction
 * mnemonics, their corresponding opcodes, and the
 * {@link AbstractInstruction} handlers responsible for executing them.</p>
 *
 * <p>The instruction set is used by the compiler and runtime to resolve
 * instructions during program compilation and execution. The
 * {@link dk.tij.registermaschine.core.runtime.Executor} retrieves the
 * appropriate instruction handler for each opcode using this interface.</p>
 *
 * <p>Instructions are typically registered using configuration definitions
 * ({@link ConfigInstruction}) which describe the mnemonic, opcode, operand
 * structure, and the handler implementations.</p>
 *
 * <p>Implementations may also prohibit certain handlers in order to
 * restrict or customise the available instruction set.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IInstructionSet {
    /**
     * Registers a new instruction in the instruction set.
     *
     * <p>The provided {@link ConfigInstruction} contains the metadata required
     * to construct the instruction handler and associate it with its mnemonic
     * and opcode.</p>
     *
     * @param configInstruction the instruction configuration to register
     */
    void registerInstruction(ConfigInstruction configInstruction);

    /**
     * Prohibits the use of a specific instruction handler.
     *
     * <p>If an instruction attempts to use a prohibited handler, registration
     * should fail or the instruction should be ignored depending on the
     * implementation.</p>
     *
     * @param instructionHandler the instruction handler class to prohibit
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
     * @param opcode the instruction mnemonic
     * @return the corresponding instruction handler
     */
    AbstractInstruction getHandler(byte opcode);

    /**
     * Returns the opcode associated with a mnemonic.
     *
     * @param mnemonic the instruction mnemonic
     * @return the corresponding opcode
     */
    byte getOpcode(String mnemonic);

    /**
     * Returns the mnemonic associated with an opcode.
     *
     * @param opcode the instruction opcode
     * @return the instruction mnemonic
     */
    String getMnemonic(byte opcode);

    /**
     * Returns whether the instruction set contains an instruction
     * with the specified mnemonic.
     *
     * @param mnemonic the instruction mnemonic
     * @return {@code true} if the instruction exists
     */
    boolean contains(String mnemonic);

    /**
     * Returns whether the instruction set contains an instruction
     * with the specified opcode.
     *
     * @param opcode the instruction opcode
     * @return {@code true} if the instruction exists
     */
    boolean contains(byte opcode);

    /**
     * Returns the list of all configured instructions.
     *
     * <p>The returned list represents the instruction definitions
     * that are currently registered in the instruction set.</p>
     *
     * @return a list of instruction configurations
     */
    List<ConfigInstruction> getInstructions();
}
