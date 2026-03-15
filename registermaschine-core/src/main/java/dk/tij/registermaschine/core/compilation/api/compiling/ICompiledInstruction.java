package dk.tij.registermaschine.core.compilation.api.compiling;

public interface ICompiledInstruction {
    int opcode();
    ICompiledOperand[] operands();
}
