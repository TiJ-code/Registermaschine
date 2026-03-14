package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.config.model.ConfigInstruction;

import java.util.List;

public interface IInstructionSet {
    void register(ConfigInstruction configInstruction, ChainedInstruction chainedInstruction);

    ConfigInstruction getConfigInstruction(int opcode);
    ConfigInstruction getConfigInstruction(String mnemonic);
    ChainedInstruction get(int opcode);

    void prohibitInstructionHandler(Class<? extends IStepHandler> stepHandlerClass);

    boolean contains(String mnemonic);
    boolean contains(int opcode);

    List<ConfigInstruction> getConfigInstructions();
    List<ChainedInstruction> getInstructions();
}
