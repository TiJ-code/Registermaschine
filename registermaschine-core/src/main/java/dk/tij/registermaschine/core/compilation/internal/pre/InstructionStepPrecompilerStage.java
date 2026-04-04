package dk.tij.registermaschine.core.compilation.internal.pre;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledStep;
import dk.tij.registermaschine.api.compilation.pre.IPrecompilerStage;
import dk.tij.registermaschine.api.config.model.ConfigInstruction;
import dk.tij.registermaschine.api.config.model.ConfigOperand;
import dk.tij.registermaschine.api.instructions.IStepHandler;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Precompiler stage that transforms {@link ConfigInstruction} step definitions
 * into executable {@link ICompiledStep} instances.
 *
 * <p>This stage resolves operand names into indices, validates step configurations,
 * and creates concrete compiled steps that can be executed efficiently at runtime.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *     <li>Build a mapping from operand names to their index positions</li>
 *     <li>Resolve step input and output operand references</li>
 *     <li>Validate handler requirements (input count and output presence)</li>
 *     <li>Create {@link ConcreteCompiledStep} instances</li>
 * </ul>
 *
 * <p>The resulting {@link ICompiledStep} array is typically passed to a later
 * precompiler stage to construct a full
 * {@link dk.tij.registermaschine.api.instructions.ChainedInstruction}.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class InstructionStepPrecompilerStage implements IPrecompilerStage<ConfigInstruction, ICompiledStep[]> {
    private static final InstructionStepPrecompilerStage INSTANCE = new InstructionStepPrecompilerStage();

    /**
     * Private constructor to prevent instantiation
     */
    private InstructionStepPrecompilerStage() {}

    /**
     * Precompiles the steps of a {@link ConfigInstruction} into an array of
     * {@link ICompiledStep}.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Builds an operand name -> index mapping</li>
     *     <li>Resolves input operand names to index arrays</li>
     *     <li>Resolves output operand name to index (or {@code -1} if none)</li>
     *     <li>Validates each step against its {@link IStepHandler}</li>
     *     <li>Constructs {@link ConcreteCompiledStep} instances</li>
     * </ol>
     *
     * @param instruction the instruction configuration to precompile
     * @return an array of compiled steps ready for execution
     *
     * @throws IllegalStateException if a step violates handler requirements
     *                               (e.g. insufficient inputs or missing output)
     */
    @Override
    public ICompiledStep[] precompile(ConfigInstruction instruction) {
        Map<String, Integer> operandIndex = buildOperandIndex(instruction.operands());

        List<ICompiledStep> compiledSteps = new ArrayList<>(instruction.steps().size());

        for (var step : instruction.steps()) {
            int[] inputs = buildInputOperandIndex(step.inputs(), operandIndex);

            int output = step.output() == null
                    ? -1
                    : operandIndex.get(step.output());

            IStepHandler handler = step.handler();

            if (inputs.length < handler.requiredInputs()) {
                throw new IllegalStateException(
                        "Step %s of instruction %s requires at least %d inputs, got %d"
                                .formatted(handler.getClass().getSimpleName(), instruction.mnemonic(),
                                           handler.requiredInputs(), inputs.length)
                );
            }

            if (handler.hasOutput() && (output < 0)) {
                throw new IllegalStateException(
                        "Step %s of instruction %s requires an output operand"
                                .formatted(handler.getClass().getSimpleName(), instruction.mnemonic())
                );
            }

            compiledSteps.add(new ConcreteCompiledStep(step.handler(), step.condition(), inputs, output));
        }

        return compiledSteps.toArray(new ICompiledStep[0]);
    }

    /**
     * Returns the singleton instance of this precompiler stage.
     *
     * @return the shared {@link InstructionStepPrecompilerStage} instance
     */
    public static InstructionStepPrecompilerStage instance() {
        return INSTANCE;
    }

    /**
     * Builds an array of input operand indices from their names.
     *
     * @param inputNames   the list of operand names used as inputs
     * @param operandIndex the mapping from operand names to indices
     * @return an array of indices corresponding to the input operands
     *
     * @throws NullPointerException if an input name does not exist in the mapping
     */
    private static int[] buildInputOperandIndex(List<String> inputNames, Map<String, Integer> operandIndex) {
        int[] inputs = new int[inputNames.size()];

        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = operandIndex.get(inputNames.get(i));
        }

        return inputs;
    }

    /**
     * Builds a mapping from operand names to their index positions.
     *
     * <p>The index corresponds to the position of operand in the
     * {@link ConfigInstruction#operands()} list.</p>
     *
     * @param operands the list of instruction operands
     * @return a map from operand name to its index
     */
    private static Map<String, Integer> buildOperandIndex(List<ConfigOperand> operands) {
        Map<String, Integer> operandIndex = new HashMap<>();

        for (int i = 0; i < operands.size(); i++) {
            operandIndex.put(operands.get(i).name(), i);
        }

        return operandIndex;
    }
}
