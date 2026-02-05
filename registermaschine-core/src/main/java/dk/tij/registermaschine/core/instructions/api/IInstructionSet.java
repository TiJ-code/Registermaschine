package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.config.ConfigInstruction;

import java.util.List;

public interface IInstructionSet {
    void registerInstruction(ConfigInstruction configInstruction);
    void prohibitInstructionHandler(Class<? extends AbstractInstruction> instructionHandler);

    AbstractInstruction getHandler(String mnemonic);
    AbstractInstruction getHandler(byte opcode);

    byte getOpcode(String mnemonic);
    String getMnemonic(byte opcode);

    boolean contains(String mnemonic);
    boolean contains(byte mnemonic);

    List<?> getInstructions();
}
