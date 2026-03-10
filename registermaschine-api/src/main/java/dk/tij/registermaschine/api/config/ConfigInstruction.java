package dk.tij.registermaschine.api.config;

import dk.tij.registermaschine.api.instructions.AbstractInstruction;

import java.util.List;

/**
 * Configuration for a single instruction in the Registermaschine runtime.
 *
 * <p>Includes the instruction mnemonic, description, opcode, operand
 * definitions, and the runtime handler implementing the instruction logic.</p>
 *
 * @param mnemonic the textual name of the instruction (e.g., ADD, MOV)
 * @param description a human-readable description of the instruction
 * @param opcode the numeric opcode used internally for execution
 * @param operands the list of operand configurations expected by this instruction
 * @param handler the {@link AbstractInstruction} instance that performs the operation
 */
public record ConfigInstruction(String mnemonic, String description,
                                byte opcode, List<ConfigOperand> operands, AbstractInstruction handler) {}
