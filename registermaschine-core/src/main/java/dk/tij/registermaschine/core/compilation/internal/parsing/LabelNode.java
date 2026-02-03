package dk.tij.registermaschine.core.compilation.internal.parsing;

public class LabelNode extends AbstractSyntaxTreeNode {
    private static final String LABEL_FORMAT = "<%s>, ";

    public final String name;

    public LabelNode(String name, int line) {
        super(line);
        this.name = name;
    }

    @Override
    public String toString() {
        String labelFormat = String.format(LABEL_FORMAT, name);
        return String.format(AST_NODE_PRINT_FORMAT, "LabelNode", labelFormat, line);
    }
}
