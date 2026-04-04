package dk.tij.registermaschine.api.compilation.compiling;

import java.util.List;

/**
 * Represents a compiled program as an ordered sequence of
 * {@link ICompiledInstruction compiled instructions}.
 *
 * <p>This interface provides both sequential and indexed access
 * to instructions by extending {@link List}. The iteration order
 * corresponds to the execution order of the program.</p>
 *
 * <p>Modifiability of the underlying collection is implementation-dependent.
 * Consumers should not assume that the program is mutable unless explicitly specified.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICompiledProgram extends Iterable<ICompiledInstruction>, List<ICompiledInstruction> {
}
