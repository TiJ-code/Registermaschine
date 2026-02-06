/**
 * Concrete implementations for the Registermaschine compilation pipeline.
 *
 * <p>This package provides the reference implementations of the compilation pipeline:
 * {@link dk.tij.registermaschine.core.compilation.ConcreteLexer},
 * {@link dk.tij.registermaschine.core.compilation.ConcreteParser}, and
 * {@link dk.tij.registermaschine.core.compilation.ConcreteCompiler}.</p>
 *
 * <p>These classes implement the stable interfaces defined in
 * {@link dk.tij.registermaschine.core.compilation.api}, and can be used directly
 * or extended to create custom compilation behaviour.</p>
 *
 * <p>While the public interfaces in {@code core.compilation.api} are stable and
 * safe to depend on, the concrete implementations in this package may evolve
 * over time and are not guaranteed to remain backwards compatible.</p>
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.compilation;