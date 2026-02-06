package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ICompiler;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledOperand;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledProgram;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.config.ConfigOperand;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledInstruction;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteInstructionNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteOperandNode;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.ArrayList;
import java.util.List;

public final class ConcreteCompiler implements ICompiler {
    @Override
    public ICompiledProgram compile(ISyntaxTree tree, IInstructionSet instructionSet) {
        List<ICompiledInstruction> program = new ArrayList<>();

        for (ISyntaxTreeNode node : tree) {
            if (node instanceof ConcreteInstructionNode instr) {
                System.out.println(instructionSet.getInstructions());
                ConfigInstruction config = instructionSet.getInstructions().stream()
                        .filter(c -> c.mnemonic().equalsIgnoreCase(instr.instruction()))
                        .findFirst().orElseThrow(() -> new RuntimeException("Unknown: " + instr.instruction()));

                System.out.println(config);

                ICompiledOperand[] finalOperands = mergeOperands(config.operands(), instr.operands);

                AbstractInstruction handler = instructionSet.getHandler(instr.instruction());
                handler.validate(finalOperands);

                program.add(new ConcreteCompiledInstruction(config.opcode(), finalOperands));
            }
        }

        return new ConcreteCompiledProgram(program);
    }

    private ICompiledOperand[] mergeOperands(List<ConfigOperand> template, List<ConcreteOperandNode> userNodes) {
        ICompiledOperand[] result = new ICompiledOperand[template.size()];

        int userIdx = 0;

        for (int i = 0; i < template.size(); i++) {
            ConfigOperand t = template.get(i);

            if (t.isImplicit()) {
                result[i] = parseInternalValue(t);
            } else {
                if (userIdx >= userNodes.size()) {
                    throw new RuntimeException("Missing operand for template at index " + i);
                }
                result[i] = parseUserValue(userNodes.get(userIdx++), t.concept());
            }
        }

        if (userIdx < userNodes.size()) {
            throw new RuntimeException("Too many operands provided for this instruction definition.");
        }

        return result;
    }

    private ICompiledOperand parseInternalValue(ConfigOperand op) {
        ICompiledOperand result;
        String value = op.value();
        if (op.type() == ConfigOperand.Type.REGISTER && value.toLowerCase().startsWith("r")) {
            result = new ConcreteCompiledOperand(ConfigOperand.Type.REGISTER, op.concept(),
                                                 Integer.parseInt(value.substring(1)));
        } else {
            result = new ConcreteCompiledOperand(ConfigOperand.Type.IMMEDIATE, ConfigOperand.Concept.OPERAND,
                                                 Integer.decode(value));
        }
        return result;
    }

    private ICompiledOperand parseUserValue(ConcreteOperandNode node, ConfigOperand.Concept concept) {
        ICompiledOperand result;
        if (node.isRegister) {
            result = new ConcreteCompiledOperand(ConfigOperand.Type.REGISTER, concept,
                                                 Integer.parseInt(node.value().substring(1)));
        } else {
            result = new ConcreteCompiledOperand(ConfigOperand.Type.IMMEDIATE, ConfigOperand.Concept.OPERAND,
                                                 Integer.decode(node.value()));
        }
        return result;
    }
}
