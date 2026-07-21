package dk.tij.registermaschine.api.compilation.pre;

/**
 * Represents a single stage in precompilation pipeline.
 *
 * <p>A precompiler stage transforms an input of type {@code I} into an output type {@code O}.
 * Multiple stages can be chained together to form a pipeline.</p>
 *
 * <p>Typical use cases include:</p>
 * <ul>
 *     <li>Validating {@link dk.tij.registermaschine.api.config.model.ConfigInstruction} before compilation.</li>
 *     <li>Transforming a {@link dk.tij.registermaschine.api.config.model.ConfigInstruction} into
 *     intermediate representations such as {@link dk.tij.registermaschine.api.compilation.compiling.ICompiledStep}{@code []}</li>
 *     <li>Generating final executable instruction representations such as
 *     {@link dk.tij.registermaschine.api.instructions.ChainedInstruction}</li>
 * </ul>
 *
 * @param <I> the input type this stage consumes
 * @param <O> the output type this stage produces
 *
 * @since 2.0.0
 * @author TiJ
 */
public interface IPrecompilerStage<I, O> {
    /**
     * Executes the precompilation logic of this stage.
     *
     * @param input the input data for this precompiler stage
     * @return the result of the precompilation, could be consumed by the next stage in the pipeline
     */
    O precompile(I input);
}
