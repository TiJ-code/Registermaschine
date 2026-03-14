package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstructionPlan;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;

public record ConcreteCompiledInstructionPlan(int opcode, ICompiledStep[] steps)
       implements ICompiledInstructionPlan {}
