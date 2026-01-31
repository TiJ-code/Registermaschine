package dk.tij.registermaschine.core;

public interface ExecutionContext {
    void addListener(ExecutionContextListener listener);
    void removeListener(ExecutionContextListener listener);

    int getRegisterCount();
    int getRegister(int index);
    void setRegister(int index, int value);

    default int getAccumulator() {
        return getRegister(0);
    }
    default void setAccumulator(int value) {
        setRegister(0, value);
    }

    int getProgrammeCounter();
    void setProgrammeCounter(int pc);

    void startExecution();
    void stopExecution();

    boolean isHalted();
    boolean getNegativeFlag();
    boolean getZeroFlag();
    boolean getOverflowFlag();
    byte getExitCode();

    void setFlags(boolean negative, boolean zero, boolean overflow);
    void setExitCode(byte code);

    void output(int value);
    int input();
}
