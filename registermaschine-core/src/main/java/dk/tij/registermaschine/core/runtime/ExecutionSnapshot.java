package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

import java.util.Map;

/**
 * Immutable snapshot of an {@link dk.tij.registermaschine.core.runtime.api.IExecutionContext}
 * at a specific moment in time.
 *
 * <p>An {@link ExecutionSnapshot} captures the relevant runtime state changes of the
 * Registermaschine so that external systems such as user interfaces, debugging or
 * monitoring tools can inspect the machine state without directly interacting
 * with the live execution context.</p>
 *
 * <p>The snapshot contains the current programme counter, machine status flags,
 * exit code, optional output value, and the set of registers that have changed
 * since the previous snapshot.</p>
 *
 * <p>The {@code registers} map does <b>not</b> necessarily contain all registers.
 * Instead, it contains only the registers whose values were modified since the
 * last snapshot. Observers are expected to maintain their own register state
 * and apply these changes incrementally.</p>
 *
 * <p>Snapshots are usually created by
 * {@link dk.tij.registermaschine.core.runtime.api.IExecutionContext#snapshotAndClearDirty()}
 * which also clears the internal "dirty" tracking after the snapshot is taken.</p>
 *
 * <p>Because this type is implemented as a {@code record}, all fields are
 * immutable once the snapshot has been created.</p>
 *
 * @param programmeCounter the current programme counter value
 * @param registers a map containing only registers that changed since the
 *                  previous snapshot, indexed by register number
 * @param negative the current state of the negative flag
 * @param zero the current state of the zero flag
 * @param overflow the current state of the overflow flag
 * @param exitCode the program exit code if execution has terminated
 * @param output the most recent output value produced by the program,
 *               or {@code null} if no output occurred since the last snapshot
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ExecutionSnapshot(int programmeCounter,
                                Map<Integer, Integer> registers,
                                boolean negative,
                                boolean zero,
                                boolean overflow,
                                byte exitCode,
                                Integer output) {
}
