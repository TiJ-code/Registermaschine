package tij.registermaschine.api.compilation.compiling;

/**
 * Identifies a compilation stage that accepts a compiled instruction factory.
 *
 * @since 2.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface ICompiledInstructionFactoryAware {
    /**
     * Sets the factory used to create compiled instructions.
     *
     * @param factory the compiled instruction factory to use
     */
    void setCompiledInstructionFactory(ICompiledInstructionFactory factory);
}
