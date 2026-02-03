package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;


import java.util.ArrayList;
import java.util.List;

public class CompiledProgram extends ArrayList<ICompiledInstruction> implements ICompiledProgram {
    public CompiledProgram() {}

    public CompiledProgram(List<ICompiledInstruction> instructions) {
        super(instructions);
    }

}
