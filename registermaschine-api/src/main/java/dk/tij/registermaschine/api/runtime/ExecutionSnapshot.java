package dk.tij.registermaschine.api.runtime;

import java.util.Map;

/**
 * Immutable snapshot of an {@link IExecutionContext} at a specific point in time.
 *
 * <p>An {@link ExecutionSnapshot} captures the runtime state of the
 * Registermaschine, allowing external tools (UI, debuggers, monitoring)
 * to inspect the machine state without modifying the live context.</p>
 *
 * <p>The snapshot contains the programme counter, machine flags,
 * exit code, optional output, and only the registers that changed
 * since the previous snapshot.</p>
 *
 * <p>Observers are expected to maintain their own register state
 * and apply these changes incrementally, as the {@code registers} map
 * may not include all registers.</p>
 *
 * <p>Snapshots are typically created via
 * {@link IExecutionContext#snapshotAndClearDirty()}, which also
 * clears the internal "dirty" tracking.</p>
 *
 * @param programmeCounter the current programme counter
 * @param registers a map of registers that changed since the last snapshot
 * @param negative the current state of the negative flag
 * @param zero the current state of the zero flag
 * @param overflow the current state of the overflow flag
 * @param exitCode the program exit code, if execution has terminated
 * @param output the most recent output value, or {@code null} if none
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
