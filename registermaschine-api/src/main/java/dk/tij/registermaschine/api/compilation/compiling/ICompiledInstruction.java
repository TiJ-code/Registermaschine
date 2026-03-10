package dk.tij.registermaschine.api.compilation.compiling;

/**
 * Represents a compiled instruction with a fixed opcode
 * and its associated operands.
 *
 * <p>Compiled instructions are created by the compiler
 * after parsing the source code. Operands are pre-resolved to
 * allow efficient execution.</p>
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
