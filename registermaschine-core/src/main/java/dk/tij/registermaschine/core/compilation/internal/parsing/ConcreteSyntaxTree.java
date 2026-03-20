package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTreeNode;

import java.util.ArrayList;
import java.util.List;

public class ConcreteSyntaxTree extends ArrayList<ISyntaxTreeNode> implements ISyntaxTree {
    public ConcreteSyntaxTree(List<ISyntaxTreeNode> nodes) {
        super(nodes);
    }
}
