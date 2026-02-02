package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.parsing.AbstractSyntaxTreeNode;

import java.util.ArrayList;
import java.util.List;

public class AbstractSyntaxTree extends ArrayList<AbstractSyntaxTreeNode> implements Iterable<AbstractSyntaxTreeNode> {
    public AbstractSyntaxTree(List<AbstractSyntaxTreeNode> nodes) {
        super(nodes);
    }
}
