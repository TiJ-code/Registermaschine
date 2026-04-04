package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Represents a compiled instruction consisting of an opcode
 * and fixed set of operands.
 *
 * <p>This interface is part of the public compilation contract and
 * is consumed by the execution layer. Implementations are expected
 * to provide operands in a form that allows efficient runtime access,
 * but the exact compilation process is not defined by this API.</p>
 *
 * <p>The meaning of the opcode and interpretation of operands are
 * defined by the active instruction set.</p>
 *
 * @since 1.0.0
 */
public interface ICompiledInstruction {
    /**
     * Returns the opcode for this instruction.
     *
     * @return the opcode
     */
    byte opcode();

    /**
     * Returns the compiled operands associated with this instruction.
     *
     * @return the compiled operands
     */
    ICompiledOperand[] operands();
}
