package dk.tij.registermaschine.core.compilation.internal.instructions;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.config.ConfigOperand;
import dk.tij.registermaschine.core.config.ConfigStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionCompiler {
    public CompiledInstructionPlan compile(ConfigInstruction instruction) {
        Map<String, Integer> operandIndex = buildOperandIndex(instruction.operands());

        List<CompiledStep> compiledSteps = new ArrayList<>(instruction.steps().size());

        for (ConfigStep step : instruction.steps()) {
            int[] inputs = step.inputs()
                    .stream()
                    .mapToInt(name -> {
                        Integer idx = operandIndex.get(name);
                        if (idx == null) {
                            throw new IllegalStateException("Instruction %s step references unknown input operand: '%s'"
                                    .formatted(instruction.mnemonic(), name));
                        }
                        return idx;
                    })
                    .toArray();

            int output = step.output() == null
                    ? -1
                    : operandIndex.get(step.output());

            compiledSteps.add(
                    new CompiledStep(step.handler(), inputs, output, step.condition())
            );
        }

        return new CompiledInstructionPlan(
                instruction.opcode(),
                compiledSteps.toArray(new CompiledStep[0])
        );
    }

    private Map<String, Integer> buildOperandIndex(List<ConfigOperand> operands) {
        Map<String, Integer> operandIndex = new HashMap<>();

        for (int i = 0; i < operands.size(); i++) {
            operandIndex.put(operands.get(i).name(), i);
        }

        return operandIndex;
    }
}
