package dk.tij.registermaschine.core.compilation.internal.parsing;

import java.util.List;

/**
 * Represents a specific operation or instruction in the AST.
 *
 * <p>An instruction node contains the mnemonic (e.g. "MOV") and
 * a list of its associated operands.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConcreteInstructionNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String INSTRUCTION_FORMAT = "instruction='%s', operands=%s, ";

    /**
     * The list of operands (registers or immediates) target by this instruction
     */
    public final List<ConcreteOperandNode> operands;

    public ConcreteInstructionNode(String instruction, List<ConcreteOperandNode> operands, int line) {
        super(instruction, line);
        this.operands = operands;
    }

    /**
     * @return The mnemonic string of the instruction
     */
    public final String instruction() {
        return value;
    }

    @Override
    public String toString() {
        String instructionFormat = String.format(INSTRUCTION_FORMAT, instruction(), operands.toString());
        return String.format(AST_NODE_PRINT_FORMAT, "InstructionNode", instructionFormat, line);
    }
}
