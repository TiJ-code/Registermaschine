package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTreeNode;

/**
 * The base class for all nodes within the Abstract Syntax Tree (AST).
 *
 * <p>This abstract class provides common properties for all syntax elements,
 * such as their original line number and their underlying string value.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public abstract class ConcreteAbstractSyntaxTreeNode implements ISyntaxTreeNode {
    /**
     * The format string used for consistent {@link #toString} output across sub-nodes.
     */
    protected static final String AST_NODE_PRINT_FORMAT = "%s[%sline=%d]";

    /**
     * The 1-based line number in the source code where this node originated.
     */
    public final int line;
    /**
     * The raw string value associated with this node (e.g., mnemonic name or literal)
     */
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