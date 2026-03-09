package dk.tij.registermaschine.core.compilation.internal.instructions;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.config.ConfigOperand;
import dk.tij.registermaschine.core.config.ConfigStep;

import java.util.ArrayList;
import java.util.List;

public class InstructionCompiler {
    public CompiledInstructionPlan compile(ConfigInstruction instruction) {
        String[] operandNames = instruction.operands().stream().map(ConfigOperand::name).toArray(String[]::new);

        List<CompiledStep> compiledSteps = new ArrayList<>(instruction.steps().size());

        for (ConfigStep step : instruction.steps()) {
            List<String> stepInputs = step.inputs();
            int[] inputs = new int[stepInputs.size()];

            for (int i = 0; i < stepInputs.size(); i++) {
                String name = stepInputs.get(i);
                int idx = findOperandIndex(instruction, name, operandNames);
                inputs[i] = idx;
            }

            int output = step.output() == null
                    ? -1
                    : findOperandIndex(instruction, step.output(), operandNames);

            compiledSteps.add(new CompiledStep(step.handler(), inputs, output));
        }

        return new CompiledInstructionPlan(
                instruction.opcode(),
                compiledSteps.toArray(new CompiledStep[0])
        );
    }

    private static int findOperandIndex(ConfigInstruction instruction, String name, String[] operands) {
        for (int i = 0; i < operands.length; i++) {
            if (operands[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Instruction %s has unknown operand: '%s'"
                .formatted(instruction.mnemonic(), name));
    }
}
