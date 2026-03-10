package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandType;

/**
 * Configuration for a single operand in an instruction.
 *
 * <p>Each operand has a type (e.g. {@link OperandType#REGISTER}, {@link OperandType#LABEL}, {@link OperandType#IMMEDIATE}),
 * a conceptual role ({@link OperandConcept#OPERAND}, {@link OperandConcept#RESULT}, {@link OperandConcept#TARGET}),
 * and an optional implicit value.</p>
 *
 * @param type the data type of the operand
 * @param concept the conceptual role of the operand in the instructions
 * @param value an optional literal value; if present, the operand is considered implicit
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConfigOperand(OperandType type, OperandConcept concept, String value) {
    /**
     * Determines if the operand has an implicit value.
     *
     * @return {@code true} if {@link #value()} is not {@code null}
     */
    public boolean isImplicit() {
        return value != null;
    }
}
