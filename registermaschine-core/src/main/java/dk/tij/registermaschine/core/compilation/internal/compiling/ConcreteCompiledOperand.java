package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandType;

public record ConcreteCompiledOperand(OperandType type, OperandConcept concept, int value)
        implements ICompiledOperand {}
