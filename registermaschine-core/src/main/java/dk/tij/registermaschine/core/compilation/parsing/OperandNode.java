package dk.tij.registermaschine.core.compilation.parsing;

public class OperandNode extends AbstractSyntaxTreeNode {
    private static final String OPERAND_FORMAT = "(%s) value=%s, ";

    public final String value;
    public final boolean isRegister;

    public OperandNode(String value, boolean isRegister, int line) {
        super(line);
        this.value = value;
        this.isRegister = isRegister;
    }

    @Override
    public String toString() {
        String operandFormat = String.format(OPERAND_FORMAT, isRegister ? "R" : "I", value);
        return String.format(AST_NODE_PRINT_FORMAT, "OperandNode", operandFormat, line);
    }
}
