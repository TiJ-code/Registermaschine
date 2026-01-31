package dk.tij.registermaschine.core;

public interface ExecutionContext {
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

    boolean getNegativeFlag();
    boolean getZeroFlag();
    boolean getOverflowFlag();

    void updateFlags(int[] operands, int operandCount, Integer result);
    void setFlags(boolean negative, boolean zero, boolean overflow);

    void output(int value);
    int input();
}
