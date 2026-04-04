package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Represents an operand used by compiled instruction.
 *
 * <p>An operand consists of:</p>
 * <ul>
 *     <li>a {@link OperandType type}, defining how the value is interpreted</li>
 *     <li>a {@link OperandConcept concept}, describing its role within the instruction</li>
 *     <li>a resolved integer value</li>
 * </ul>
 *
 * <p>The interpretation of the value depends on the operand type
 * and the execution environment. For example, it may represent a
 * register index, an immediate value, or a resolved reference.</p>
 *
 * <p>This interface does not define how operands are created or resolved;
 * it only defines the contract required for execution.</p>
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
