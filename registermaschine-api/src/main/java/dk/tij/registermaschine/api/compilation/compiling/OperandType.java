package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Defines how the value of an operand is interpreted during execution.
 *
 * <ul>
 *     <li>{@link #REGISTER} – value represents a register identifier</li>
 *     <li>{@link #IMMEDIATE} – value is used directly as a constant</li>
 *     <li>{@link #LABEL} – value represents a resolved control-flow target</li>
 * </ul>
 *
 * <p>The exact interpretation of the value is defined by the execution
 * environment and instruction set.</p>
 *
 * <p>This enum does not define how values are produced or resolved.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public enum OperandType {
    REGISTER, IMMEDIATE, LABEL
}
