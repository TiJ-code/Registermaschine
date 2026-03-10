/**
 * Defines the core interfaces and base classes for the Registermaschine's instruction set.
 * <p>
 *     This package provides the architectural framework for implementing operations.
 *     The primary components include:
 * </p>
 * <ul>
 *     <li>{@link dk.tij.registermaschine.core.instructions.api.IInstructionSet}:
 *     A registry that maps mnemonics and opcodes to their respective handler.</li>
 *     <li>{@link dk.tij.registermaschine.core.instructions.api.AbstractInstruction}:
 *     The base class for all operations, providing lifecycle methods for validation,
 *     conditional checking, and execution.</li>
 * </ul>
 * <b>Instruction Lifecycle</b>
 * Each instruction follows a strict <b>Validate-Check-Execute</b> sequence managed by
 * the {@link dk.tij.registermaschine.core.runtime.Executor}.
 *
 * @since 1.0.0
 * @author TiJ
 */
package dk.tij.registermaschine.core.instructions.api;