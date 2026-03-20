package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTreeNode;

public abstract class ConcreteAbstractSyntaxTreeNode implements ISyntaxTreeNode {
    protected static final String AST_NODE_PRINT_FORMAT = "%s[%sline=%d]";

    public final int line;
    protected final String value;

    protected ConcreteAbstractSyntaxTreeNode(String value, int line) {
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format(AST_NODE_PRINT_FORMAT, "ASTNode", "", line);
    }
}