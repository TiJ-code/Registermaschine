package dk.tij.registermaschine.ui.listeners;

import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.runtime.api.IExecutionContextListener;
import dk.tij.registermaschine.ui.ui.JavaScriptBridge;
import dk.tij.registermaschine.ui.utils.AlertTypes;

public final class ExecutionListener implements IExecutionContextListener {
    private final JavaScriptBridge bridge;

    public ExecutionListener(JavaScriptBridge bridge) {
        this.bridge = bridge;
    }

    @Override public void setContext(IExecutionContext ctx) {}
    @Override public void onExecutionStarted() {}
    @Override public void onExecutionStopped() {}
    @Override public void onRegisterChanged(int index, int newValue) {}
    @Override public void onFlagChanged(boolean negative, boolean zero, boolean overflow) {}
    @Override public void onExitCodeChanged(byte newValue) {}
    @Override public void onProgrammeCounterChanged(int newPc) {}
    @Override public void onOutput(int value) {}

    @Override
    public void onMaxJumpsReached() {
        bridge.transmit().toast(
                "Execution Halted",
                String.format("Max jump limit (%d) reached. Possible infinite loop detected.", CoreConfig.MAX_JUMPS),
                AlertTypes.WARNING
        );
    }
}
