package dk.tij.registermaschine.api.runtime;

public interface IExecutionContextListener {
    void setContext(IExecutionContext ctx);

    void onExecutionStarted();
    void onExecutionStopped();

    void onRegisterChanged(int index, int newValue);
    void onFlagChanged(boolean negative, boolean zero, boolean overflow);
    void onExitCodeChanged(byte newValue);
    void onProgrammeCounterChanged(int newPc);
    void onMaxJumpsReached();
    void onOutput(int value);
    default void onInputRequested() {}
}
