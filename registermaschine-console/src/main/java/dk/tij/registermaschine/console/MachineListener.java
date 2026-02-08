package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;

import java.util.Scanner;

public class MachineListener implements IExecutionContextListener {
    private final Scanner scanner;
    private IExecutionContext context;

    public MachineListener(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void setContext(IExecutionContext ctx) {
        this.context = ctx;
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
        System.out.printf("N: %-5s  |  Z: %-5s  |  V: %-5s%n", negative, zero, overflow);
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
    public void onInputRequested() {
        context.provideInput(Integer.decode(scanner.nextLine()));
    }
}
