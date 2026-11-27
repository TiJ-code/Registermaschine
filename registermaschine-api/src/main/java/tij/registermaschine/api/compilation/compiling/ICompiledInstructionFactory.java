package tij.registermaschine.api.compilation.compiling;

/**
 * Creates compiled instructions for a compilation stage.
 *
 * @since 2.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface ICompiledInstructionFactory {
    /**
     * Creates a compiled instruction with the given opcode and operands.
     *
     * @param opcode the instruction opcode
     * @param operands the compiled operands
     * @return the created instruction
     */
    ICompiledInstruction create(int opcode, ICompiledOperand[] operands);
}
