package dk.tij.registermaschine.core.parser.ast;

import java.util.List;

public class InstructionNode extends ASTNode {
    private static final String INSTRUCTION_FORMAT = "instruction='%s', operands=%s, ";

    public final String instruction;
    public final List<OperandNode> operands;

    public InstructionNode(String instruction, List<OperandNode> operands, int line) {
        super(line);
        this.instruction = instruction;
        this.operands = operands;
    }

    @Override
    public String toString() {
        String instructionFormat = String.format(INSTRUCTION_FORMAT, instruction, operands.toString());
        return String.format(AST_NODE_PRINT_FORMAT, "InstructionNode", instructionFormat, line);
    }
}
