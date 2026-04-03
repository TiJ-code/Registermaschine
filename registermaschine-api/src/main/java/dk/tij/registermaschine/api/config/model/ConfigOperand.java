package dk.tij.registermaschine.api.config.model;

import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;

/**
 * Describes an operand definition within an instruction configuration.
 *
 * <p>An operand is defined by its {@link OperandType type}, its
 * {@link OperandConcept conceptual role}, and an optional implicit value.</p>
 *
 * <p>If a value is provided, the operand is considered implicit and does
 * not need to be specified explicitly in source input.</p>
 *
 * @param type the operand type
 * @param concept the semantic role of the operand
 * @param value an optional value representing an implicit operand
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConfigOperand(OperandType type, OperandConcept concept, String value) {
    /**
     * Returns whether this operand has an implicit value.
     *
     * @return {@code true} if a value is defined, otherwise {@code false}
     */
    public boolean isImplicit() {
        return value != null;
    }
}
