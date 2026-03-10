/**
 * Provides the concrete implementation of the machine's runtime environment and
 * instruction execution engine.
 * <p>
 *     This package implements the "Fetch-Decode-Execute" cycle. It consists of:
 * </p>
 * <ul>
 *     <li>{@link dk.tij.registermaschine.core.runtime.ConcreteExecutionContext}:
 *     The concrete implementation of the virtual hardware, managing physical storage for registers
 *     and the logic for the flags.</li>
 *     <li>{@link dk.tij.registermaschine.core.runtime.Executor}:
 *     The orchestration engine that iterates through a compiled program, manages
 *     the Programme Counter, and delegates opcode handling to the instruction set.</li>
 * </ul>
 * <b>Execution Cycle:</b>
 * The {@code Executor} maintains a tight loop that persists until the {@code ExecutionContext}
 * signals a {@code HALT} or the Program Counter exceeds the bounds of the code segment.
 * @see dk.tij.registermaschine.core.runtime.api.IExecutionContext
 *
 * @since 1.0.0
 * @author TiJ
 */
package dk.tij.registermaschine.core.runtime;