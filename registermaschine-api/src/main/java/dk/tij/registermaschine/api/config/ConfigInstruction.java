package dk.tij.registermaschine.api.config;

import dk.tij.registermaschine.api.instructions.AbstractInstruction;

import java.util.List;

public record ConfigInstruction(String mnemonic, String description,
                                byte opcode, List<ConfigOperand> operands, AbstractInstruction handler) {}
