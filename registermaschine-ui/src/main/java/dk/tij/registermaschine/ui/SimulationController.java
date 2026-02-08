package dk.tij.registermaschine.ui;

import dk.tij.registermaschine.core.compilation.Pipeline;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import dk.tij.registermaschine.core.runtime.ConcreteExecutionContext;
import dk.tij.registermaschine.core.runtime.ExecutionSnapshot;
import dk.tij.registermaschine.core.runtime.Executor;
import dk.tij.registermaschine.ui.ui.JavaScriptBridge;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationController {
    private final JavaScriptBridge bridge;
    private final ConcreteInstructionSet set;

    private ConcreteExecutionContext context;
    private Executor runtime;
    private ScheduledExecutorService uiScheduler;
    private Thread emulationThread;


    public SimulationController(JavaScriptBridge bridge) {
        if (bridge == null) throw new IllegalStateException("The JS Bridge cannot be null!!");
        this.bridge = bridge;

        this.set = new ConcreteInstructionSet();

        CoreConfigParser.init();
        CoreConfigParser.parseDefaultInstructionSet(set);
    }

    public void handleRunRequest(String sourceCode) {
        this.context = new ConcreteExecutionContext();
        this.context.setInputRequestCallback(() -> bridge.transmit().requestInput());

        this.runtime = new Executor(context, set);

        startUiLoop();
        resetUI();

        try {
            ICompiledProgram program = Pipeline.compile(sourceCode, set);
            runtime.setProgram(program);

            emulationThread = new Thread(() -> {
                runtime.run();
                bridge.transmit().notifyProgramFinished();
            }, "EmulatorThread");
            emulationThread.start();
        } catch (Exception e) {
            Platform.runLater(() -> e.printStackTrace());
        }
    }

    public void handleStopRequest() {
        if (runtime != null) {
            runtime.stop();;
        }

        if (emulationThread != null && emulationThread.isAlive() && Thread.currentThread() != emulationThread) {
            try {
                emulationThread.join();
            } catch (InterruptedException _) {}
        }

        stopUiLoop();

        if (bridge != null) {
            bridge.transmit().notifyProgramFinished();
        }
    }

    public void provideInput(int value) {
        if (context != null)
            context.provideInput(value);
    }

    private void startUiLoop() {
        if (uiScheduler == null || uiScheduler.isShutdown())
            uiScheduler = Executors.newSingleThreadScheduledExecutor();

        uiScheduler.scheduleAtFixedRate(() -> {
            if (context == null) return;

            ExecutionSnapshot snapshot = context.snapshotAndClearDirty();

            Platform.runLater(() -> {
                bridge.transmit().updateFromSnapshot(snapshot);
            });
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void stopUiLoop() {
        if (uiScheduler != null && !uiScheduler.isShutdown())
            uiScheduler.shutdownNow();
    }

    private void resetUI() {
        if (context == null) return;

        bridge.transmit().updateFromSnapshot(context.resetSnapshot());
    }
}
