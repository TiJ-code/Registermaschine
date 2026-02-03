package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;

public abstract class AbstractSyntaxTreeNode implements ISyntaxTreeNode {
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