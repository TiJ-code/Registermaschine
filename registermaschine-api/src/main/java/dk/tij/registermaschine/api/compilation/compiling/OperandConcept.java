package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Describes the semantic role of operand within an instruction.
 *
 * <p>The concept defines how an operand is used during execution,
 * independent of its {@link OperandType type}.</p>
 *
 * <ul>
 *     <li>{@link #RESULT} - destination for the result of an operation</li>
 *     <li>{@link #OPERAND} - input value participating in a computation</li>
 *     <li>{@link #TARGET} - destination used for control flow (e.g. jump or branch)</li>
 * </ul>
 *
 * <p>Not all instructions use all concepts. The meaning and required
 * combination of concepts are defined by the instruction set.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public enum OperandConcept {
    RESULT, OPERAND, TARGET
}
