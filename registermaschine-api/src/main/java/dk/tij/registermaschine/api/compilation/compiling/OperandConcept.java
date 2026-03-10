package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Defines the conceptual role of a compiled operand within an instruction
 *
 * <ul>
 *     <li>{@link #RESULT} - operand where the result of the operation is going</li>
 *     <li>{@link #OPERAND} - source operand participating in computation</li>
 *     <li>{@link #TARGET} - operand used as a jump/branch target</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
public enum OperandConcept {
    RESULT, OPERAND, TARGET
}
