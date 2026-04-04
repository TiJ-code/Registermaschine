package dk.tij.registermaschine.core.compilation.internal.pre;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.compilation.pre.IPrecompilerStage;
import dk.tij.registermaschine.api.config.model.ConfigInstruction;
import dk.tij.registermaschine.api.instructions.ChainedInstruction;

import java.util.Arrays;

public final class InstructionPrecompilerStage implements IPrecompilerStage<ConfigInstruction, ChainedInstruction> {
    private static final InstructionPrecompilerStage INSTANCE = new InstructionPrecompilerStage();

    private InstructionPrecompilerStage() {}

    @Override
    public ChainedInstruction precompile(ConfigInstruction instruction) {
        ICompiledStep[] steps = InstructionStepPrecompilerStage.instance().precompile(instruction);

        int minOperandCount = Arrays.stream(steps)
                .mapToInt(step -> step.inputIndices().length + (step.outputIndex() >= 0 ? 1 : 0))
                .max()
                .orElse(0);

        if (instruction.operands().size() < minOperandCount) {
            throw new IllegalStateException(
                    "Instruction %s defines %d operands but requires at least %d for its steps"
                            .formatted(instruction.mnemonic(), instruction.operands().size(), minOperandCount)
            );
        }

        return new ChainedInstruction(instruction.operands().size(), instruction.condition(), steps);
    }

    public static InstructionPrecompilerStage instance() {
        return INSTANCE;
    }
}
