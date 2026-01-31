package dk.tij.registermaschine.core.parser.ast;

public abstract class ASTNode {
    protected static final String AST_NODE_PRINT_FORMAT = "%s[%sline=%d]";

    public final int line;

    protected ASTNode(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format(AST_NODE_PRINT_FORMAT, "ASTNode", "", line);
    }
}