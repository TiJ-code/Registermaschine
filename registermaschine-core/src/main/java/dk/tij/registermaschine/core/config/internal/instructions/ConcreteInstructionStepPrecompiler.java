package dk.tij.registermaschine.core.config.internal.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledStep;
import dk.tij.registermaschine.core.config.api.instructions.IInstructionPrecompiler;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.config.model.ConfigOperand;
import dk.tij.registermaschine.core.config.model.ConfigStep;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcreteInstructionStepPrecompiler implements IInstructionPrecompiler<ICompiledStep[]> {
    private static final ConcreteInstructionStepPrecompiler INSTANCE = new ConcreteInstructionStepPrecompiler();

    private ConcreteInstructionStepPrecompiler() {}

    @Override
    public ICompiledStep[] precompile(ConfigInstruction instruction) {
        Map<String, Integer> operandIndex = buildOperandIndex(instruction.operands());

        List<ICompiledStep> compiledSteps = new ArrayList<>(instruction.steps().size());

        for (ConfigStep step : instruction.steps()) {
            int[] inputs = buildInputOperandIndex(step.inputs(), operandIndex);

            int output = step.output() == null
                    ? -1
                    : operandIndex.get(step.output());

            IStepHandler handler = step.handler();

            if (inputs.length < handler.requiredInputs()) {
                throw new IllegalStateException(
                        "Step %s requires at least %d input operands, got %d"
                                .formatted(handler.getClass().getSimpleName(), handler.requiredInputs(), inputs.length)
                );
            }

            if (handler.hasOutput() && (output < 0)) {
                throw new IllegalStateException(
                        "Step %s requires an output operand"
                                .formatted(handler.getClass().getSimpleName())
                );
            }

            compiledSteps.add(new ConcreteCompiledStep(step.handler(), step.condition(), inputs, output));
        }

        return compiledSteps.toArray(new ICompiledStep[0]);
    }

    public static IInstructionPrecompiler<ICompiledStep[]> instance() {
        return INSTANCE;
    }

    private static int[] buildInputOperandIndex(List<String> inputNames, Map<String, Integer> operandIndex) {
        int[] inputs = new int[inputNames.size()];

        for (int i = 0; i < inputNames.size(); i++) {
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
