package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;
import dk.tij.registermaschine.core.config.CoreConfig;

import java.util.*;

public final class ConcreteExecutionContext implements IExecutionContext {
    private static final byte FLAG_RUNNING = 0b0001,
            FLAG_ZERO = 0b0010,
            FLAG_NEGATIVE = 0b0100,
            FLAG_OVERFLOW = 0b1000;

    private final int[] registers;
    private int programmeCounter;

    private byte jumpCounter;
    private byte exitCode;
    private byte flags;

    private final boolean[] dirtyRegisters;
    private volatile boolean dirtyFlags, dirtyPc, dirtyOutput;
    private volatile Integer lastOutput;

    public ConcreteExecutionContext() {
        this.registers = new int[CoreConfig.REGISTERS];
        this.dirtyRegisters = new boolean[CoreConfig.REGISTERS];
        this.programmeCounter = 0;
        this.exitCode = 0;
        this.flags = 0;
    }

    @Override
    public int getRegister(int index) {
        return registers[index];
    }

    @Override
    public void setRegister(int index, int value) {
        registers[index] = value;
        dirtyRegisters[index] = true;
        listeners.forEach(l -> l.onRegisterChanged(index, value));
    }

    @Override
    public int getProgrammeCounter() {
        return programmeCounter;
    }

    @Override
    public void resetProgrammeCounter() {
        programmeCounter = 0;
        dirtyPc = true;
    }

    @Override
    public void step() {
        programmeCounter++;
        dirtyPc = true;
        listeners.forEach(l -> l.onProgrammeCounterChanged(programmeCounter));
    }

    @Override
    public void setProgrammeCounter(int pc) {
        if (jumpCounter >= maxJumpCounter()) return;
        programmeCounter = pc;
        dirtyPc = true;
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
        dirtyFlags = true;
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
        lastOutput = value;
        dirtyOutput = true;
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
