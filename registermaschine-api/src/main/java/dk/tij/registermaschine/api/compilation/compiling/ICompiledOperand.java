package dk.tij.registermaschine.api.compilation.compiling;

public interface ICompiledOperand {
    OperandType type();
    OperandConcept concept();
    int value();
}
