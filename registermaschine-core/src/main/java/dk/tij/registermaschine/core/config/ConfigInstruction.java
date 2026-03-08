package dk.tij.registermaschine.core.config;

import java.util.List;

public record ConfigInstruction(String mnemonic, String description,
                                byte opcode, List<ConfigOperand> operands, List<ConfigStep> steps) {}
