package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.config.model.ConfigInstruction;

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
    void register(ConfigInstruction configInstruction, ChainedInstruction chainedInstruction);

    ConfigInstruction getConfigInstruction(int opcode);
    ConfigInstruction getConfigInstruction(String mnemonic);
    ChainedInstruction get(int opcode);

    void prohibitStepHandler(Class<? extends IStepHandler> stepHandlerClass);

    boolean contains(String mnemonic);
    boolean contains(int opcode);

    List<ConfigInstruction> getConfigInstructions();
    List<ChainedInstruction> getInstructions();
}
