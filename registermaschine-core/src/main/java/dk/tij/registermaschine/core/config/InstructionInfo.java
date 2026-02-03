package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;

public record InstructionInfo(String mnemonic, String description, byte opcode, AbstractInstruction handler) {}
