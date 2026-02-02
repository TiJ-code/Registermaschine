package dk.tij.registermaschine.core.cpu;

import dk.tij.registermaschine.core.runtime.ExecutionContext;
import dk.tij.registermaschine.core.runtime.ExecutionContextListener;
import dk.tij.registermaschine.core.config.Config;

import java.util.ArrayList;
import java.util.List;

public class BasicExecutionContext implements ExecutionContext {
    private static final byte FLAG_RUNNING  = 0b0001,
                              FLAG_ZERO     = 0b0010,
                              FLAG_NEGATIVE = 0b0100,
                              FLAG_OVERFLOW = 0b1000;
    
    private final List<ExecutionContextListener> listeners;

    private final int[] registers;
    private int programmeCounter;

    private byte exitCode;
    private byte flags;

    public BasicExecutionContext() {
        this.listeners = new ArrayList<>();

        this.registers = new int[Config.REGISTERS];
        this.programmeCounter = 0;
        this.exitCode = 0;
        this.flags = 0;
    }

    @Override
    public void addListener(ExecutionContextListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ExecutionContextListener listener) {
        listeners.remove(listener);
    }

    @Override
    public int getRegisterCount() {
        return registers.length;
    }

    @Override
    public int getRegister(int index) {
        return registers[index];
    }

    @Override
    public void setRegister(int index, int value) {
        registers[index] = value;
        listeners.forEach(l -> l.onRegisterChanged(index, value));
    }

    @Override
    public int getProgrammeCounter() {
        return programmeCounter;
    }

    @Override
    public void setProgrammeCounter(int pc) {
        programmeCounter = pc;
        listeners.forEach(l -> l.onProgrammeCounterChanged(pc));
    }

    @Override
    public void startExecution() {
        flags |= FLAG_RUNNING;
        listeners.forEach(ExecutionContextListener::onExecutionStarted);
    }

    @Override
    public void stopExecution() {
        flags &= ~FLAG_RUNNING;
        listeners.forEach(ExecutionContextListener::onExecutionStopped);
    }

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

    @Override
    public void setFlags(boolean negative, boolean zero, boolean overflow) {
        flags &= FLAG_RUNNING;
        flags |= negative ? FLAG_NEGATIVE : 0;
        flags |= zero ? FLAG_ZERO : 0;
        flags |= overflow ? FLAG_OVERFLOW : 0;
        listeners.forEach(l -> l.onFlagChanged(negative, zero, overflow));
    }

    @Override
    public void setExitCode(byte code) {
        this.exitCode = code;
        listeners.forEach(l -> l.onExitCodeChanged(code));
    }

    @Override
    public void output(int value) {
        listeners.forEach(l -> l.onOutput(value));
    }

    @Override
    public int input() {
        for (ExecutionContextListener l : listeners) {
            Integer value = l.onInputRequested();
            if (value != null)
                return value;
        }
        throw new UnsupportedOperationException("Input cannot not be provided by listeners");
    }
}
