package dk.tij.registermaschine.core.compilation.api.compiling;

public interface ICompiledInstruction {
    int opcode();
    ICompiledStep[] steps();
    ICompiledOperand[] operands();
}
