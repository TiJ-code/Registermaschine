package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledInstruction;
import dk.tij.registermaschine.api.compilation.compiling.ICompiledProgram;


import java.util.ArrayList;
import java.util.List;

public class ConcreteCompiledProgram extends ArrayList<ICompiledInstruction> implements ICompiledProgram {
    public ConcreteCompiledProgram() {}

    public ConcreteCompiledProgram(List<ICompiledInstruction> instructions) {
        super(instructions);
    }
}
