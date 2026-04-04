package dk.tij.registermaschine.core.compilation.internal.parsing;

/**
 * Represents a jump target or anchor within the source code.
 *
 * <p>Labels allows the programmer to mark specific points in the code to be
 * referenced by branching or jumping instructions.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConcreteLabelNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String LABEL_FORMAT = "<%s>, ";

    public ConcreteLabelNode(String name, int line) {
        super(name, line);
    }

    /**
     * @return The name of the label
     */
    public final String label() {
        return value;
    }

    @Override
    public String toString() {
        String labelFormat = String.format(LABEL_FORMAT, label());
        return String.format(AST_NODE_PRINT_FORMAT, "LabelNode", labelFormat, line);
    }
}
