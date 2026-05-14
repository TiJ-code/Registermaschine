package dk.tij.registermaschine.api.runtime;

/**
 * Listener interface for observing changes in an {@link IExecutionContext}.
 *
 * <p>Listeners can react to runtime events such as register updates,
 * flags, programme counter changes, I/O, and execution lifecycle events.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IExecutionContextListener {
    /**
     * Assigns the execution context that this listener observes.
     *
     * <p>This method is called when the listener is registered or
     * removed from an {@link IExecutionContext}. Implementations may store
     * the reference if they need direct access to the runtime state.</p>
     *
     * @param ctx the associated execution context, or {@code null} if the
     *            listener has been detached
     */
    void setContext(IExecutionContext ctx);

    /**
     * Invoked when program execution begins.
     *
     * <p>This event is triggered when the execution context enters the
     * running state</p>
     */
    void onExecutionStarted();

    /**
     * Invoked when the program execution stops.
     *
     * <p>This event is triggered when on program termination,
     * an explicit stop request or an interruption of the executor.</p>
     */
    void onExecutionStopped();

    /**
     * Invoked when the value of a register changes.
     *
     * @param index the index of the modified register
     * @param newValue the new value stored in the register
     */
    void onRegisterChanged(int index, int newValue);

    /**
     * Invoked when the machine status flags change.
     *
     * @param negative the current state of the negative flag
     * @param zero the current state of the zero flag
     * @param overflow the current state of the overflow flag
     */
    void onFlagChanged(boolean negative, boolean zero, boolean overflow);

    /**
     * Invoked when the program exit code changes.
     *
     * @param newValue the new exit code
     */
    void onExitCodeChanged(byte newValue);

    /**
     * Invoked when the programme counter changes.
     *
     * @param newPc the updated programme counter value
     */
    void onProgrammeCounterChanged(int newPc);

    /**
     * Invoked when the maximum allowed number of jumps has been reached.
     *
     * <p>This event may indicate a potential infinite loop or invalid
     * control flow within the executing program.</p>
     */
    void onMaxJumpsReached();

    /**
     * Invoked when the program produces an output value.
     *
     * @param value the output value
     */
    void onOutput(int value);

    void onAddressUpdated(long address, byte value);

    /**
     * Invoked when input is requested.
     *
     * <p>Default implementation does nothing. Override if user input or prompt is required.</p>
     */
    default void onInputRequested() {}
}
