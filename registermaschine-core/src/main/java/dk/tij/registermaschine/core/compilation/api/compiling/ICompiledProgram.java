package dk.tij.registermaschine.core.compilation.api.compiling;

import java.util.List;

/**
 * Represents a compiled program consisting of a sequence of
 * {@link ICompiledInstruction compiled instructions}.
 *
 * <p>This interface extends both {@link Iterable} and {@link List}
 * for convenient iteration and random access.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICompiledProgram extends Iterable<ICompiledInstruction>, List<ICompiledInstruction> {
}
