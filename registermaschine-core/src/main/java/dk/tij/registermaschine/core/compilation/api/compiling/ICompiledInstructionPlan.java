package dk.tij.registermaschine.core.compilation.api.compiling;

public interface ICompiledInstructionPlan {
    int opcode();
    ICompiledStep[] steps();
}
