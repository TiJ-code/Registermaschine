package dk.tij.registermaschine.api.compilation.parsing;

/**
 * Represents a syntax tree produced by the {@link dk.tij.registermaschine.core.compilation.api.IParser}.
 *
 * <p>The tree consists of nodes implementing {@link ISyntaxTreeNode}.
 * It can be iterated over to traverse the tree in source order.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ISyntaxTree extends Iterable<ISyntaxTreeNode> {}
