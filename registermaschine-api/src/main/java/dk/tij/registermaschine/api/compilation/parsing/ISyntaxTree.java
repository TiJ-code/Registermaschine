package dk.tij.registermaschine.api.compilation.parsing;

/**
 * Represents a syntax tree produced during the parsing phase.
 *
 * <p>A syntax tree consists of {@link ISyntaxTreeNode nodes} that describe
 * the structure of the parsed input. The exact structure and hierarchy
 * are implementation-dependent.</p>
 *
 * <p>This interface provides iteration over the contained nodes. The
 * iteration order typically follows the source order, but this is not
 * strictly required unless specified by the implementation.</p>
 *
 * <p>This API does not define how syntax trees are created or traversed
 * beyond simple iteration.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ISyntaxTree extends Iterable<ISyntaxTreeNode> {}
