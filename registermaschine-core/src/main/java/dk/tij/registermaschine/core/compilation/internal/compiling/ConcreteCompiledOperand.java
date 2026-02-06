package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.config.ConfigOperand;

public record ConcreteCompiledOperand(ConfigOperand.Type type, ConfigOperand.Concept concept, int value)
        implements ICompiledOperand {}
