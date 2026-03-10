package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Defines the type of compiled operand.
 *
 * <ul>
 *     <li>{@link #REGISTER} - a CPU register index</li>
 *     <li>{@link #IMMEDIATE} - a literal numeric value</li>
 *     <li>{@link #LABEL} - a (e.g., symbolic) jump target resolved to an address</li>
 * </ul>
 */
public enum OperandType {
    REGISTER, IMMEDIATE, LABEL
}
