package dk.tij.registermaschine.core.config.model;

import dk.tij.registermaschine.core.conditions.api.ICondition;

import java.util.List;

public record ConfigInstruction(String mnemonic, String description,
                                int opcode, ICondition condition,
                                List<ConfigOperand> operands, List<ConfigStep> steps) {}
