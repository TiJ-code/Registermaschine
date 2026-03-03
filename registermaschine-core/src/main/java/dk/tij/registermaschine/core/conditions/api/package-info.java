/**
 * Provides the fundamental interfaces for conditional instruction execution.
 * <p>
 *     This package defined the {@link dk.tij.registermaschine.core.conditions.api.ICondition}
 *     which acts as a predicate for the {@link dk.tij.registermaschine.core.instructions.api.AbstractInstruction}.
 * </p>
 * <h3>Condition Lifecycle</h3>
 * When an instruction is fetched by the Executor, the {@code shouldExecute} method
 * of the assigned condition is evaluated. If it returns {@code true}, the instruction logic is performed;
 * otherwise, the instruction is treated as a {@code NOP} (No Operation).
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.conditions.api;