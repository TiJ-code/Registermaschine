package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The root container for a parsed program.
 *
 * <p>This structure stores an ordered sequence of {@link ISyntaxTreeNode}s,
 * typically consisting of instructions and labels, representing the logical
 * flow of the program.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class ConcreteSyntaxTree extends ArrayList<ISyntaxTreeNode> implements ISyntaxTree {
    /**
     * Constructs a syntax tree from a provided list of nodes.
     *
     * @param nodes The ordered nodes that make up the program.
     */
    public ConcreteSyntaxTree(List<ISyntaxTreeNode> nodes) {
        super(nodes);
    }
}
