package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.compiling.CompiledInstruction;

import java.util.ArrayList;
import java.util.List;

public class CompiledProgram extends ArrayList<CompiledInstruction> implements Iterable<CompiledInstruction> {
    public CompiledProgram() {}

    public CompiledProgram(List<CompiledInstruction> instructions) {
        super(instructions);
    }
}
