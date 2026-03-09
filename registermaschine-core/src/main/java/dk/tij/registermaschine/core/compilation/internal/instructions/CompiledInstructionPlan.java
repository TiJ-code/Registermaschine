package dk.tij.registermaschine.core.compilation.internal.instructions;

public record CompiledInstructionPlan(byte opcode, CompiledStep[] steps) {}
