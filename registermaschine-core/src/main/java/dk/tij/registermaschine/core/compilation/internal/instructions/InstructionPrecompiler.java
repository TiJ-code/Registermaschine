package dk.tij.registermaschine.core.compilation.internal.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledStep;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledStep;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.config.model.ConfigOperand;
import dk.tij.registermaschine.core.config.model.ConfigStep;

import java.util.ArrayList;
import java.util.List;

public class InstructionPrecompiler {
    private InstructionPrecompiler() {}

    public static ICompiledStep[] compile(ConfigInstruction instruction) {
        String[] operandNames = instruction.operands().stream().map(ConfigOperand::name).toArray(String[]::new);

        List<ICompiledStep> compiledSteps = new ArrayList<>(instruction.steps().size());

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

            compiledSteps.add(new ConcreteCompiledStep(step.handler(), step.condition(), inputs, output));
        }

        return compiledSteps.toArray(new ICompiledStep[0]);
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
