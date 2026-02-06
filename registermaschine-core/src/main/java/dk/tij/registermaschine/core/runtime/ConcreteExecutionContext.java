package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;
import dk.tij.registermaschine.core.config.CoreConfig;

import java.util.ArrayList;
import java.util.List;

public final class ConcreteExecutionContext implements IExecutionContext {
    private static final byte FLAG_RUNNING  = 0b0001,
                              FLAG_ZERO     = 0b0010,
                              FLAG_NEGATIVE = 0b0100,
                              FLAG_OVERFLOW = 0b1000;
    
    private final List<IExecutionContextListener> listeners;

    private final int[] registers;
    private int programmeCounter;

    private byte jumpCounter;
    private byte exitCode;
    private byte flags;

    public ConcreteExecutionContext() {
        this.listeners = new ArrayList<>();

        this.registers = new int[CoreConfig.REGISTERS];
        this.programmeCounter = 0;
        this.exitCode = 0;
        this.flags = 0;
    }

    @Override
    public void addListener(IExecutionContextListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IExecutionContextListener listener) {
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
    public void resetProgrammeCounter() {
        programmeCounter = 0;
    }

    @Override
    public void step() {
        programmeCounter++;
        listeners.forEach(l -> l.onProgrammeCounterChanged(programmeCounter));
    }

    @Override
    public void setProgrammeCounter(int pc) {
        if (jumpCounter >= maxJumpCounter()) return;
        programmeCounter = pc;
        listeners.forEach(l -> l.onProgrammeCounterChanged(pc));
    }

    @Override
    public void startExecution() {
        flags |= FLAG_RUNNING;
        listeners.forEach(IExecutionContextListener::onExecutionStarted);
    }

    @Override
    public void stopExecution() {
        flags &= ~FLAG_RUNNING;
        listeners.forEach(IExecutionContextListener::onExecutionStopped);
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
    public void incJumpCounter() {
        jumpCounter++;
    }

    @Override
    public int maxJumpCounter() {
        return CoreConfig.MAX_JUMPS;
    }

    @Override
    public void output(int value) {
        listeners.forEach(l -> l.onOutput(value));
    }

    @Override
    public int input() {
        for (IExecutionContextListener l : listeners) {
            Integer value = l.onInputRequested();
            if (value != null)
                return value;
        }
        throw new UnsupportedOperationException("Input cannot not be provided by listeners");
    }
}
