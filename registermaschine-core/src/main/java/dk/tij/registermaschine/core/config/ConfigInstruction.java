package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;

import java.util.List;

public record ConfigInstruction(String mnemonic, String description,
                                byte opcode, List<ConfigOperand> operands, AbstractInstruction handler) {}
