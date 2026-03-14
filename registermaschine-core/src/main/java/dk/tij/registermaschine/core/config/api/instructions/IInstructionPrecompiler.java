package dk.tij.registermaschine.core.config.api.instructions;

import dk.tij.registermaschine.core.config.model.ConfigInstruction;

public interface IInstructionPrecompiler<T> {
    T precompile(ConfigInstruction instruction);
}
