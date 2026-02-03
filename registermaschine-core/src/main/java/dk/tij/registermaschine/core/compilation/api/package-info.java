/**
 * Public API for the Registermaschine compilation pipeline.
 *
 * <p>This package contains only stable interfaces for the compilation pipeline.
 * including {@link dk.tij.registermaschine.core.compilation.api.ILexer},
 * {@link dk.tij.registermaschine.core.compilation.api.IParser} and
 * {@link dk.tij.registermaschine.core.compilation.api.ICompiler}.</p>
 *
 * <p>Concrete implementations are provided in<br/>
 * {@link dk.tij.registermaschine.core.compilation.ConcreteLexer},<br/>
 * {@link dk.tij.registermaschine.core.compilation.ConcreteParser} and<br/>
 * {@link dk.tij.registermaschine.core.compilation.ConcreteCompiler}.<br/>
 * Users may implement their own versions by implementing the provided interfaces.</p>
 *
 * <p>All interfaces in this package are stable and safe to depend on. Implementations
 * in other packages may change without notice.</p>
 */
package dk.tij.registermaschine.core.compilation.api;