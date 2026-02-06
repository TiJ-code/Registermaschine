package dk.tij.registermaschine.core.runtime.api;

public interface IExecutionContext {
    void addListener(IExecutionContextListener listener);
    void removeListener(IExecutionContextListener listener);

    int getRegisterCount();
    int getRegister(int index);
    void setRegister(int index, int value);

    int getProgrammeCounter();
    void resetProgrammeCounter();
    void incProgrammeCounter();
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

    void incJumpCounter();
    int maxJumpCounter();

    void output(int value);
    int input();
}
