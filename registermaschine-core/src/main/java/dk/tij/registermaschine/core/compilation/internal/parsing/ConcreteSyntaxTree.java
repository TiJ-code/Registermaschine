package dk.tij.registermaschine.core.compilation.internal.parsing;

import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;

import java.util.ArrayList;
import java.util.List;

public class ConcreteSyntaxTree extends ArrayList<ISyntaxTreeNode> implements ISyntaxTree {
    public ConcreteSyntaxTree(List<ISyntaxTreeNode> nodes) {
        super(nodes);
    }
}
