package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;

import java.util.ArrayList;
import java.util.List;

public class AbstractSyntaxTree extends ArrayList<ISyntaxTreeNode> implements ISyntaxTree {
    public AbstractSyntaxTree(List<ISyntaxTreeNode> nodes) {
        super(nodes);
    }
}
