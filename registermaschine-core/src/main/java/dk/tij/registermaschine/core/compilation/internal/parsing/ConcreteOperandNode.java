package dk.tij.registermaschine.core.compilation.internal.parsing;

public class ConcreteOperandNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String OPERAND_FORMAT = "(%s) value=%s, ";

    public final boolean isRegister;
    public final boolean isAddress;

    public ConcreteOperandNode(String value, boolean isRegister, boolean isAddress, int line) {
        super(value, line);
        this.isRegister = isRegister;
        this.isAddress = isAddress;
    }

    public final String value() {
        return value;
    }

    @Override
    public String toString() {
        String operandFormat = String.format(OPERAND_FORMAT, isRegister ? "R" : "I", value());
        return String.format(AST_NODE_PRINT_FORMAT, "OperandNode", operandFormat, line);
    }
}
