package dk.tij.registermaschine.core;

public interface ExecutionContextListener {
    void onExecutionStarted();
    void onExecutionStopped();

    void onRegisterChanged(int index, int newValue);
    void onFlagChanged(boolean negative, boolean zero, boolean overflow);
    void onExitCodeChanged(byte newValue);
    void onProgrammeCounterChanged(int newPc);
    void onOutput(int value);
    default Integer onInputRequested() {
        return null;
    }
}
