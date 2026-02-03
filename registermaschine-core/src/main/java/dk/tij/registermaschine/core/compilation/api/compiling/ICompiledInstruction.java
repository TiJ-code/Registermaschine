package dk.tij.registermaschine.core.compilation.api.compiling;

public interface ICompiledInstruction {
    byte opcode();
    int[] operands();
}
