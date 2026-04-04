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

public final class InstructionStepPrecompilerStage implements IPrecompilerStage<ConfigInstruction, ICompiledStep[]> {
    private static final InstructionStepPrecompilerStage INSTANCE = new InstructionStepPrecompilerStage();

    private InstructionStepPrecompilerStage() {}

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

    public static InstructionStepPrecompilerStage instance() {
        return INSTANCE;
    }

    private static int[] buildInputOperandIndex(List<String> inputNames, Map<String, Integer> operandIndex) {
        int[] inputs = new int[inputNames.size()];

        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = operandIndex.get(inputNames.get(i));
        }

        return inputs;
    }

    private static Map<String, Integer> buildOperandIndex(List<ConfigOperand> operands) {
        Map<String, Integer> operandIndex = new HashMap<>();

        for (int i = 0; i < operands.size(); i++) {
            operandIndex.put(operands.get(i).name(), i);
        }

        return operandIndex;
    }
}
