package dk.tij.registermaschine.core.compilation.internal.parsing;

public class ConcreteOperandNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String OPERAND_FORMAT = "(%s) value=%s, ";

    public final boolean isRegister;

    public ConcreteOperandNode(String value, boolean isRegister, int line) {
        super(value, line);
        this.isRegister = isRegister;
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
