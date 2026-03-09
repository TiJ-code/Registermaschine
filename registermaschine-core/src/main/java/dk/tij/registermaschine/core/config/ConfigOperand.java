package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandType;

public record ConfigOperand(String name, OperandType type, OperandConcept concept, String value) {
    public boolean isImplicit() {
        return value != null;
    }
}
