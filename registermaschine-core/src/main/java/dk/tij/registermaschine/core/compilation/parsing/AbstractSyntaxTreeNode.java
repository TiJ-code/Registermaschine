package dk.tij.registermaschine.core.compilation.parsing;

public abstract class AbstractSyntaxTreeNode {
    protected static final String AST_NODE_PRINT_FORMAT = "%s[%sline=%d]";

    public final int line;

    protected AbstractSyntaxTreeNode(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format(AST_NODE_PRINT_FORMAT, "ASTNode", "", line);
    }
}