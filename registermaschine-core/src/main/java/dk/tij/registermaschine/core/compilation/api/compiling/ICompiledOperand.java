package dk.tij.registermaschine.core.compilation.api.compiling;

public interface ICompiledOperand {
    OperandType type();
    OperandConcept concept();
    int value();
}
