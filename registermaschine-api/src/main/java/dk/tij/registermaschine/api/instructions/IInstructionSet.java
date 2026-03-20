package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.config.ConfigInstruction;

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

    List<ConfigInstruction> getInstructions();
}
