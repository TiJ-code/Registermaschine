package dk.tij.registermaschine.api.config;

import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;

public record ConfigOperand(OperandType type, OperandConcept concept, String value) {
    public boolean isImplicit() {
        return value != null;
    }
}
