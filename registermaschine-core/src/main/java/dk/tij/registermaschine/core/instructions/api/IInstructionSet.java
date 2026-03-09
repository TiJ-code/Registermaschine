package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.internal.instructions.CompiledInstructionPlan;
import dk.tij.registermaschine.core.config.ConfigInstruction;

import java.util.List;

public interface IInstructionSet {
    void registerInstruction(ConfigInstruction configInstruction);

    void prohibitInstructionHandler(Class<? extends AbstractInstruction> instructionHandler);

    ConfigInstruction getInstruction(String mnemonic);
    ConfigInstruction getInstruction(byte opcode);

    @Deprecated(since = "2.0.0")
    AbstractInstruction getHandler(String mnemonic);
    @Deprecated(since = "2.0.0")
    AbstractInstruction getHandler(byte opcode);

    byte getOpcode(String mnemonic);
    String getMnemonic(byte opcode);

    boolean contains(String mnemonic);
    boolean contains(byte opcode);

    @Deprecated(since = "2.0.0", forRemoval = true)
    List<ConfigInstruction> getInstructions();

    CompiledInstructionPlan getPlan(byte opcode);
}
