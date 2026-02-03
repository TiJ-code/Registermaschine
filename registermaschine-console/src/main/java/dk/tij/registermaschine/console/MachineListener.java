package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;

import java.util.Scanner;

public class MachineListener implements IExecutionContextListener {
    private final Scanner scanner;

    public MachineListener() {
        this.scanner = null;
    }

    public MachineListener(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void onExecutionStarted() {}

    @Override
    public void onExecutionStopped() {
        System.out.println("Stopped Execution");
    }

    @Override
    public void onRegisterChanged(int index, int newValue) {
        System.out.println("r" + index + ":\t" + newValue);
    }

    @Override
    public void onFlagChanged(boolean negative, boolean zero, boolean overflow) {
        System.out.println("N: " + negative + "\tZ: " + zero + "\tV: " + overflow);
    }

    @Override
    public void onExitCodeChanged(byte newValue) {
        System.out.println("Exit Code: " + newValue);
    }

    @Override
    public void onProgrammeCounterChanged(int newPc) {}

    @Override
    public void onOutput(int value) {
        System.out.println("OUT: " + value);
    }

    @Override
    public Integer onInputRequested() {
        if (scanner == null) return IExecutionContextListener.super.onInputRequested();
        return Integer.decode(scanner.nextLine());
    }
}
