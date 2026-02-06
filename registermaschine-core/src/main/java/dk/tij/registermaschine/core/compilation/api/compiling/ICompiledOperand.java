package dk.tij.registermaschine.core.compilation.api.compiling;

import dk.tij.registermaschine.core.config.ConfigOperand;

public interface ICompiledOperand {
    ConfigOperand.Type type();
    ConfigOperand.Concept concept();
    int value();
}
