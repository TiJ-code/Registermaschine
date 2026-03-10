package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;
import dk.tij.registermaschine.core.config.CoreConfig;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Default implementation of {@link IExecutionContext} used by the runtime
 * system of the Registermaschine.
 *
 * <p>This class maintains a complete mutable execution state of the virtual
 * machine including:</p>
 *
 * <ul>
 *     <li>Register values</li>
 *     <li>The programme counter</li>
 *     <li>Status flags (negative, zero, overflow, running)</li>
 *     <li>Exit code</li>
 *     <li>Input/output handling</li>
 * </ul>
 *
 * <p>The context also tracks <em>dirty state</em> for registers, flags,
 * programme counter and output values. This allows external systems such as
 * user interfaces to obtain incremental state updates using
 * {@link #snapshotAndClearDirty()} instead of re-reading the entire machine
 * state.</p>
 *
 * <p>Input operations are handled through a {@link BlockingQueue} so that
 * execution can safely block while waiting for user input. When input is
 * requested, registered {@link IExecutionContextListener}s are notified.</p>
 *
 * <p>This implementation is designed to be used together with {@link Executor},
 * which drives the instruction execution loop.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConcreteExecutionContext implements IExecutionContext {
    private static final byte FLAG_RUNNING = 0b0001,
            FLAG_ZERO = 0b0010,
            FLAG_NEGATIVE = 0b0100,
            FLAG_OVERFLOW = 0b1000;

    private final BlockingQueue<Integer> inputQueue = new LinkedBlockingQueue<>();
    private Runnable inputRequestCallback;

    private final int[] registers;
    private int programmeCounter;

    private int jumpCounter;
    private byte exitCode;
    private byte flags;

    private final boolean[] dirtyRegisters;
    private volatile boolean dirtyFlags, dirtyPc, dirtyOutput;
    private volatile Integer lastOutput;

    /**
     * Creates a new execution context with a register array sized according
     * to {@link CoreConfig#REGISTERS}.
     *
     * <p>All registers, flags, and counters are initialised to zero.</p>
     */
    public ConcreteExecutionContext() {
        this.registers = new int[CoreConfig.REGISTERS];
        this.dirtyRegisters = new boolean[CoreConfig.REGISTERS];
        this.programmeCounter = 0;
        this.exitCode = 0;
        this.flags = 0;
    }

    /**
     * Returns the value stored in the specified register.
     *
     * @param index the register index
     * @return the current value of the register
     */
    @Override
    public int getRegister(int index) {
        return registers[index];
    }

    /**
     * Updates the value of a register.
     *
     * <p>The register is marked as dirty and all registered
     * {@link IExecutionContextListener}s are notified.</p>
     *
     * @param index the register index
     * @param value the value to store in the register
     */
    @Override
    public void setRegister(int index, int value) {
        registers[index] = value;
        dirtyRegisters[index] = true;
        listeners.forEach(l -> l.onRegisterChanged(index, value));
    }

    /**
     * Returns the current programme counter.
     *
     * @return the index of the next instruction to execute
     */
    @Override
    public int getProgrammeCounter() {
        return programmeCounter;
    }

    /**
     * Resets the programme counter to {@code 0}
     *
     * <p>The programme counter is marked as dirty.</p>
     */
    @Override
    public void resetProgrammeCounter() {
        programmeCounter = 0;
        dirtyPc = true;
    }

    /**
     * Advances the programme counter by one instruction.
     *
     * <p>This method is typically invoked by the executor before
     * executing the current instruction.</p>
     */
    @Override
    public void step() {
        programmeCounter++;
        dirtyPc = true;
        listeners.forEach(l -> l.onProgrammeCounterChanged(programmeCounter));
    }

    /**
     * Sets the programme counter to a new value.
     *
     * <p>This operation counts as a jump. If the number of jumps exceeds
     * {@link #maxJumpCounter()}, execution is stopped and listeners are
     * notified via {@link IExecutionContextListener#onMaxJumpsReached()}.</p>
     *
     * @param pc the new programme counter value
     */
    @Override
    public void setProgrammeCounter(int pc) {
        incJumpCounter();
        if (jumpCounter >= maxJumpCounter()) {
            listeners.forEach(IExecutionContextListener::onMaxJumpsReached);
            stopExecution();
            return;
        }
        programmeCounter = pc;
        dirtyPc = true;
        listeners.forEach(l -> l.onProgrammeCounterChanged(pc));
    }

    /**
     * Marks the execution context as running and notifies listeners that
     * execution has started.
     */
    @Override
    public void startExecution() {
        flags |= FLAG_RUNNING;
        listeners.forEach(IExecutionContextListener::onExecutionStarted);
    }

    /**
     * Stops execution and notifies all listeners.
     */
    @Override
    public void stopExecution() {
        flags &= ~FLAG_RUNNING;
        listeners.forEach(IExecutionContextListener::onExecutionStopped);
    }

    /**
     * Returns whether the machine is currently halted.
     *
     * @return {@code true} if execution is not running.
     */
    @Override
    public boolean isHalted() {
        return (flags & FLAG_RUNNING) == 0;
    }

    @Override
    public boolean getNegativeFlag() {
        return (flags & FLAG_NEGATIVE) > 0;
    }

    @Override
    public boolean getZeroFlag() {
        return (flags & FLAG_ZERO) > 0;
    }

    @Override
    public boolean getOverflowFlag() {
        return (flags & FLAG_OVERFLOW) > 0;
    }

    @Override
    public byte getExitCode() {
        return exitCode;
    }

    /**
     * Updates the machine status flags.
     *
     * <p>The running flag is preserved while the arithmetic flags
     * (negative, zero, overflow) are updated</p>
     *
     * @param negative whether the negative flag should be set
     * @param zero whether the zero flag should be set
     * @param overflow whether the overflow flag should be set
     */
    @Override
    public void setFlags(boolean negative, boolean zero, boolean overflow) {
        flags &= FLAG_RUNNING;
        flags |= negative ? FLAG_NEGATIVE : 0;
        flags |= zero ? FLAG_ZERO : 0;
        flags |= overflow ? FLAG_OVERFLOW : 0;
        dirtyFlags = true;
        listeners.forEach(l -> l.onFlagChanged(negative, zero, overflow));
    }

    /**
     * Sets the exit code of the program and notifies listeners.
     *
     * @param code the exit code
     */
    @Override
    public void setExitCode(byte code) {
        this.exitCode = code;
        listeners.forEach(l -> l.onExitCodeChanged(code));
    }

    @Override
    public void incJumpCounter() {
        jumpCounter++;
    }

    @Override
    public int maxJumpCounter() {
        return CoreConfig.MAX_JUMPS;
    }

    /**
     * Emits an output value from the program.
     *
     * <p>The value is stored for the next snapshot and listeners are
     * notified immediately.</p>
     *
     * @param value the value to output
     */
    @Override
    public void output(int value) {
        lastOutput = value;
        dirtyOutput = true;
        listeners.forEach(l -> l.onOutput(value));
    }

    /**
     * Requests an input value for the executing program.
     *
     * <p>Listeners are notified that input is required. The execution thread
     * blocks until a value is supplied via {@link #provideInput(int)}.</p>
     *
     * @return the provided input value
     */
    @Override
    public int input() {
        listeners.forEach(IExecutionContextListener::onInputRequested);
        notifyInputRequested();

        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execution interrupted while waiting for input!");
        }
    }

    /**
     * Supplies an input value to the execution context.
     *
     * <p>If a thread is currently waiting in {@link #input()}, it will be
     * unblocked and receive this value.</p>
     *
     * @param value the input value
     */
    @Override
    public void provideInput(int value) {
        inputQueue.offer(value);
    }

    /**
     * Notifies the registered input request callback that the executing
     * program is waiting for input.
     *
     * <p>If a callback has been registered via
     * {@link #setInputRequestCallback(Runnable)}, it will be invoked to
     * allow external systems (such as a user interface) to react to the
     * input request.</p>
     *
     * <p>If no callback is registered, this method performs no action.</p>
     */
    private void notifyInputRequested() {
        if (inputRequestCallback != null)
            inputRequestCallback.run();
    }

    /**
     * Sets a callback that will be executed whenever input is requested.
     *
     * <p>This can be used by user interfaces to trigger input prompts.</p>
     *
     * @param callback the callback to execute when input is requested
     */
    public void setInputRequestCallback(Runnable callback) {
        this.inputRequestCallback = callback;
    }

    /**
     * Creates an {@link ExecutionSnapshot} containing the current execution
     * state and all registers that have changed since previous snapshot.
     *
     * <p>After the snapshot is created, all dirty markers are cleared.</p>
     *
     * @return a snapshot representing the latest observable execution state.
     */
    @Override
    public ExecutionSnapshot snapshotAndClearDirty() {
        Integer out = dirtyOutput ? lastOutput : null;

        Map<Integer, Integer> dirtyRegs = new HashMap<>();
        for (int i = 0; i < registers.length; i++) {
            if (dirtyRegisters[i]) {
                dirtyRegs.put(i, getRegister(i));
            }
        }

        ExecutionSnapshot snapshot = new ExecutionSnapshot(
                programmeCounter,
                dirtyRegs,
                getNegativeFlag(),
                getZeroFlag(),
                getOverflowFlag(),
                getExitCode(),
                out
        );

        Arrays.fill(dirtyRegisters, false);
        dirtyFlags = false;
        dirtyPc = false;
        dirtyOutput = false;

        return snapshot;
    }
}
