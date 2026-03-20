package dk.tij.registermaschine.api.compilation.compiling;

public interface ICompiledInstruction {
    byte opcode();
    ICompiledOperand[] operands();
}
