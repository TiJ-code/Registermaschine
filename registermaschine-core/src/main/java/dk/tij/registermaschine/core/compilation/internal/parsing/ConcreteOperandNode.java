package dk.tij.registermaschine.core.compilation.internal.parsing;

/**
 * Represents a parameter passed to an instruction.
 *
 * <p>An operand can represent a literal constant (Immediate), a CPU register,
 * or a memory address reference.</p>
 */
public class ConcreteOperandNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String OPERAND_FORMAT = "(%s) value=%s, ";

    /**
     * Indicates if the operand refers to a hardware register.
     */
    public final boolean isRegister;

    /**
     * Indicates if the operand should be treated as a memory address
     */
    public final boolean isAddress;

    public ConcreteOperandNode(String value, boolean isRegister, boolean isAddress, int line) {
        super(value, line);
        this.isRegister = isRegister;
        this.isAddress = isAddress;
    }

    /**
     * @return The raw value of the operand as a string.
     */
    public final String value() {
        return value;
    }

    @Override
    public String toString() {
        String operandFormat = String.format(OPERAND_FORMAT, isRegister ? "R" : "I", value());
        return String.format(AST_NODE_PRINT_FORMAT, "OperandNode", operandFormat, line);
    }
}
