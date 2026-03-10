package dk.tij.registermaschine.api.runtime;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the runtime state of a Registermaschine during program execution.
 *
 * <p>The {@link IExecutionContext} acts as the central state container used by the
 * runtime system. It stores and manages the machine registers, program counter,
 * status flags, input/output operations, and execution lifecycle events.</p>
 *
 * <p>The {@link dk.tij.registermaschine.core.runtime.Executor} interacts with the
 * context during program execution, while instruction implementations modify the
 * machine state through the methods defined here.</p>
 *
 * <p>Implementations are responsible for maintaining internal state of the
 * virtual machine and ensuring consistent behaviour when instruction interact
 * with register, flags, or I/O.</p>
 *
 * <p>The execution context also supports listeners via {@link IExecutionContextListener}.
 * These listeners can observe execution events, such as state changes, enabling
 * debugging tools, visualisers, or UI frontends to react to runtime updates.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IExecutionContext {
    Set<IExecutionContextListener> listeners = new HashSet<>();

    /**
     * Registers an execution context listener.
     *
     * <p>The listener will receive updates related to execution state changes.
     * Once registered, the listener is associated with this context.</p>
     *
     * @param listener the listener to register
     */
    default void addListener(IExecutionContextListener listener) {
        listeners.add(listener);
        listener.setContext(this);
    }

    /**
     * Removes a previously registered execution context listener.
     *
     * <p>The listener will no longer receive execution updates and will be
     * detached from this context.</p>
     *
     * @param listener the listener to remove
     */
    default void removeListener(IExecutionContextListener listener) {
        listeners.remove(listener);
        listener.setContext(null);
    }

    /**
     * Returns the value stored in this specified register.
     *
     * @param index the register index
     * @return the value currently stored in the register
     */
    int getRegister(int index);

    /**
     * Sets the value of the specified register.
     *
     * @param index the register index
     * @param value the value to store in the register
     */
    void setRegister(int index, int value);

    /**
     * Returns the current programme counter.
     *
     * <p>The programme counter identifies the next instruction to be executed
     * within the compiled program.</p>
     *
     * @return the current programme counter
     */
    int getProgrammeCounter();

    /**
     * Resets the programme counter to its initial state.
     */
    void resetProgrammeCounter();

    /**
     * Sets the programme counter to a specific instruction index.
     *
     * @param pc the new programme counter value
     */
    void setProgrammeCounter(int pc);

    /**
     * Advances the programme counter by one instruction.
     *
     * <p>This method is typically called by the executor before instruction
     * execution.</p>
     */
    void step();

    /**
     * Advances the start of program execution.
     *
     * <p>Implementations may use this method to initialise runtime state
     * or notify listneres that execution has begun.</p>
     */
    void startExecution();

    /**
     * Signals that program execution has stopped.
     *
     * <p>This may occur due to program termination, interruption or
     * manual stopping of the executor.</p>
     */
    void stopExecution();

    /**
     * Returns whether the machine has halted.
     *
     * @return {@code true} if execution has halted, otherwise {@code false}
     */
    boolean isHalted();

    /**
     * Returns the current state of the negative flag.
     *
     * @return {@code true} if negative flag is set
     */
    boolean getNegativeFlag();

    /**
     * Returns the current state of the zero flag.
     *
     * @return {@code true} if zero flag is set
     */
    boolean getZeroFlag();

    /**
     * Returns the current state of the overflow flag.
     *
     * @return {@code true} if overflow flag is set
     */
    boolean getOverflowFlag();

    /**
     * Returns the program exit code.
     *
     * @return the exit code produced by the program.
     */
    byte getExitCode();

    /**
     * Updates the status flags of the machine.
     *
     * @param negative whether the negative flag should be set
     * @param zero whether the zero flag should be set
     * @param overflow whether the overflow flag should be set
     */
    void setFlags(boolean negative, boolean zero, boolean overflow);

    /**
     * Sets the exit code produced by the program.
     *
     * @param code the exit code
     */
    void setExitCode(byte code);

    /**
     * Increments the internal jump counter.
     *
     * <p>This counter can be used to detect excessive or infinite jump
     * operations during execution.</p>
     */
    void incJumpCounter();

    /**
     * Returns the maximum allowed jump counter value.
     *
     * <p>If the jump counter exceeds this value, execution may be considered
     * invalid or non-terminating depending on the implementation.</p>
     *
     * @return the maximum jump counter value
     */
    int maxJumpCounter();

    /**
     * Sends a value to the program output
     *
     * @param value the value to output
     */
    void output(int value);

    /**
     * Provides input to the execution context.
     *
     * <p>This method supplies a value that can later be retrieved via {@link #input()}.</p>
     *
     * @param value the input value
     */
    void provideInput(int value);

    /**
     * Retrieves an input value for an executing program.
     *
     * <p>This method may block until input becomes available.</p>
     *
     * @return the provided input value
     * @throws InterruptedException if the waiting thread is interrupted
     */
    int input() throws InterruptedException;

    /**
     * Creates a snapshot of the current execution state.
     *
     * <p>The snapshot contains relevant runtime information such as
     * register values, flags, and the programme counter. After creating
     * the snapshot, the internal "dirty" state tracking is cleared.</p>
     *
     * @return a snapshot representing the current execution state
     */
    ExecutionSnapshot snapshotAndClearDirty();
}
