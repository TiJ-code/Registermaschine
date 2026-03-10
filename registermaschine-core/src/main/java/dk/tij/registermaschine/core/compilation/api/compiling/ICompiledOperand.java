package dk.tij.registermaschine.core.compilation.api.compiling;

/**
 * Represents a compiled operand in an instruction.
 *
 * <p>An operand has a type ({@link OperandType#REGISTER}, {@link OperandType#IMMEDIATE},
 * {@link OperandType#LABEL}), a concept (what role it plays in
 * the instruction), and a resolved integer value.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICompiledOperand {
    /**
     * Returns the type of this operand.
     *
     * @return the {@link OperandType}
     */
    OperandType type();

    /**
     * Returns the conceptual role of this operand.
     *
     * @return the {@link OperandConcept}
     */
    OperandConcept concept();

    /**
     * Returns the resolved value of this operand (register index or immediate).
     * @return the resolved integer value
     */
    int value();
}
