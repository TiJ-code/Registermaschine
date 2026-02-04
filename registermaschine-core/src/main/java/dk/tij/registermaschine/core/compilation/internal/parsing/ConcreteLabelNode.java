package dk.tij.registermaschine.core.compilation.internal.parsing;

public class ConcreteLabelNode extends ConcreteAbstractSyntaxTreeNode {
    private static final String LABEL_FORMAT = "<%s>, ";

    public ConcreteLabelNode(String name, int line) {
        super(name, line);
    }

    public final String label() {
        return value;
    }

    @Override
    public String toString() {
        String labelFormat = String.format(LABEL_FORMAT, label());
        return String.format(AST_NODE_PRINT_FORMAT, "LabelNode", labelFormat, line);
    }
}
