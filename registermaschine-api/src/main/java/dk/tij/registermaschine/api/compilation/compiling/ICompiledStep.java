package dk.tij.registermaschine.api.compilation.compiling;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.IStepHandler;

/**
 * Represents a compiled, executable step within an instruction.
 *
 * <p>An {@link ICompiledStep} is the result of the precompilation phase and
 * encapsulates all information required to execute a single step at runtime.
 * It combines a {@link IStepHandler}, an optional {@link ICondition}, and
 * operand index mappings.</p>
 *
 * <p>Each step operates on a shared operand array, where:</p>
 * <ul>
 *     <li>{@link #inputIndices()} define which operands are used as inputs</li>
 *     <li>{@link #outputIndex()} defines where the result is written (if applicable)</li>
 * </ul>
 *
 * <p>The {@link ICondition}, if present, determines whether the step should
 * be executed. If the condition evaluates to {@code false}, the step is skipped.</p>
 *
 * <p>Compiled steps are typically executed sequentially as part of a
 * {@link dk.tij.registermaschine.api.instructions.ChainedInstruction}.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public interface ICompiledStep {
    /**
     * Returns the step handler responsible for executing this step.
     *
     * @return the {@link IStepHandler} associated with this step
     */
    IStepHandler handler();

    /**
     * Returns the condition that must be satisfied for this step to execute.
     *
     * <p>If the condition evaluates to {@code false}, the step will be skipped.</p>
     *
     * @return the {@link ICondition}, or {@code null} if no condition is defined
     */
    ICondition condition();

    /**
     * Returns the indices of operands used as inputs for this step.
     *
     * <p>These indices refer to positions in the instruction's operands.</p>
     *
     * @return an array of input operand indices
     */
    int[] inputIndices();

    /**
     * Returns the index of the operand used as output.
     *
     * <p>If the step does not produce an output, this value should be {@code -1}</p>
     *
     * @return the output operand index, or {@code -1} if none
     */
    int outputIndex();
}
