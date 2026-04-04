package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledInstruction;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledProgram;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete sequence of compiled instructions ready for execution.
 *
 * <p>This class extends {@link ArrayList} to provide list-like access to the
 * instruction set while fulfilling the {@link ICompiledProgram} interface.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConcreteCompiledProgram extends ArrayList<ICompiledInstruction> implements ICompiledProgram {
    /**
     * Constructs an empty compiled program.
     */
    public ConcreteCompiledProgram() {}

    /**
     * Constructs a compiled program from an existing list of instructions.
     *
     * @param instructions The list of {@link ICompiledInstruction} to include.
     */
    public ConcreteCompiledProgram(List<ICompiledInstruction> instructions) {
        super(instructions);
    }
}
