package dk.tij.registermaschine.api.config.model;

import dk.tij.registermaschine.api.conditions.ICondition;

import java.util.List;

/**
 * Describes a single instruction within a configuration.
 *
 * <p>Includes the instruction identifier, opcode, operand definitions,
 * and the associated execution logic.</p>
 *
 * <p>This is a data representation and does not define how instructions
 * are executed or instantiated.</p>
 *
 * @param mnemonic the textual identifier of the instruction (e.g. ADD, MOV)
 * @param description a human-readable description
 * @param opcode the opcode used to identify the instruction during execution
 * @param operands the operand definitions expected by the instruction
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConfigInstruction(String mnemonic, String description,
                                int opcode, ICondition condition,
                                List<ConfigOperand> operands, List<ConfigStep> steps) {}
