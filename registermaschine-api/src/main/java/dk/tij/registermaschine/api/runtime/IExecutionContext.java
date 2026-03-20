package dk.tij.registermaschine.api.runtime;

import java.util.HashSet;
import java.util.Set;

public interface IExecutionContext {
    Set<IExecutionContextListener> listeners = new HashSet<>();

    default void addListener(IExecutionContextListener listener) {
        listeners.add(listener);
        listener.setContext(this);
    }

    default void removeListener(IExecutionContextListener listener) {
        listeners.remove(listener);
        listener.setContext(null);
    }

    int getRegister(int index);
    void setRegister(int index, int value);

    int getProgrammeCounter();
    void resetProgrammeCounter();
    void setProgrammeCounter(int pc);
    void step();

    void startExecution();
    void stopExecution();

    boolean isHalted();
    boolean getNegativeFlag();
    boolean getZeroFlag();
    boolean getOverflowFlag();
    byte getExitCode();

    void setFlags(boolean negative, boolean zero, boolean overflow);
    void setExitCode(byte code);

    void incJumpCounter();
    int maxJumpCounter();

    void output(int value);
    void provideInput(int value);
    int input() throws InterruptedException;

    dk.tij.registermaschine.api.runtime.ExecutionSnapshot snapshotAndClearDirty();
}
