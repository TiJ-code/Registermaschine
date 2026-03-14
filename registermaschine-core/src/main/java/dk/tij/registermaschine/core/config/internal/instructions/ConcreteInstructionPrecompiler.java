package dk.tij.registermaschine.core.config.internal.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.config.api.instructions.IInstructionPrecompiler;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.instructions.api.ChainedInstruction;

import java.util.Arrays;

public class ConcreteInstructionPrecompiler implements IInstructionPrecompiler<ChainedInstruction> {
    private static final ConcreteInstructionPrecompiler INSTANCE = new ConcreteInstructionPrecompiler();

    private ConcreteInstructionPrecompiler() {}

    @Override
    public final ChainedInstruction precompile(ConfigInstruction instruction) {
        ICompiledStep[] steps = ConcreteInstructionStepPrecompiler.instance().precompile(instruction);

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

        return new ChainedInstruction(
                instruction.operands().size(),
                instruction.condition(),
                ConcreteInstructionStepPrecompiler.instance().precompile(instruction)
        );
    }

    public static IInstructionPrecompiler<ChainedInstruction> instance() {
        return INSTANCE;
    }
}
