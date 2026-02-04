package dk.tij.registermaschine.core.compilation.internal.parsing;

import java.util.List;

public class ConcreteInstructionNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String INSTRUCTION_FORMAT = "instruction='%s', operands=%s, ";

    public final List<ConcreteOperandNode> operands;

    public ConcreteInstructionNode(String instruction, List<ConcreteOperandNode> operands, int line) {
        super(instruction, line);
        this.operands = operands;
    }

    public final String instruction() {
        return value;
    }

    @Override
    public String toString() {
        String instructionFormat = String.format(INSTRUCTION_FORMAT, instruction(), operands.toString());
        return String.format(AST_NODE_PRINT_FORMAT, "InstructionNode", instructionFormat, line);
    }
}
