package dk.tij.registermaschine.core.compilation.internal.pre;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.compilation.pre.IPrecompilerStage;
import dk.tij.registermaschine.api.config.model.ConfigInstruction;
import dk.tij.registermaschine.api.instructions.ChainedInstruction;

import java.util.Arrays;

/**
 * Precompiler stage that transforms a {@link ConfigInstruction} into a
 * executable {@link ChainedInstruction}.
 *
 * <p>This stage represents the final step of the precompilation pipeline and
 * delegates step compilation to {@link InstructionStepPrecompilerStage} and
 * performs additional validation on the instruction as a whole.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *     <li>Invoke step precompilation to produce {@link ICompiledStep} instances</li>
 *     <li>Determine the minimum number of operands required by all step handlers</li>
 *     <li>Validate that the instruction defines a sufficient number of operands</li>
 *     <li>Create the final {@link ChainedInstruction}</li>
 * </ul>
 *
 * <p>The resulting {@link ChainedInstruction} can be executed directly at runtime.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class InstructionPrecompilerStage implements IPrecompilerStage<ConfigInstruction, ChainedInstruction> {
    private static final InstructionPrecompilerStage INSTANCE = new InstructionPrecompilerStage();

    /**
     * Private constructor to prevent instantiation
     */
    private InstructionPrecompilerStage() {}

    /**
     * Precompiles the given {@link ConfigInstruction} into a {@link ChainedInstruction} instance.
     *
     * <p>This method first compiles all steps using
     * {@link InstructionStepPrecompilerStage}, then validates that the number of
     * defined operands is sufficient for all steps.</p>
     *
     * @param instruction the instruction configuration to precompile
     * @return a fully compiled {@link ChainedInstruction}
     * @throws IllegalStateException if the instruction defines fewer operands
     *                               than required by its steps
     */
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

    /**
     * Returns the singleton instance of this precompiler stage.
     *
     * @return the shared {@link InstructionPrecompilerStage} instance
     */
    public static InstructionPrecompilerStage instance() {
        return INSTANCE;
    }
}
