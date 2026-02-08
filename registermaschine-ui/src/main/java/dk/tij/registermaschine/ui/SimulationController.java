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
    private final ConcreteExecutionContext context;
    private final Executor runtime;
    private ScheduledExecutorService uiScheduler;

    private final ConcreteInstructionSet set;

    public SimulationController(JavaScriptBridge bridge) {
        this.bridge = bridge;

        this.set = new ConcreteInstructionSet();

        CoreConfigParser.init();
        CoreConfigParser.parseDefaultInstructionSet(set);

        this.context = new ConcreteExecutionContext();
        this.context.setInputRequestCallback(() -> {
            bridge.transmit().requestInput();
        });
        this.runtime = new Executor(context, set);
        this.uiScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void handleRunRequest(String sourceCode) {
        startUiLoop();

        Thread emulationThread = new Thread(() -> {
           try {
               ICompiledProgram program = Pipeline.compile(sourceCode, set);

               runtime.setProgram(program);
               runtime.setSpeed(20);
               runtime.run();

               bridge.stopProgram();
           } catch (Exception e) {
               Platform.runLater(() -> {
                   e.printStackTrace();
                   bridge.stopProgram();
               });
           }
        });

        emulationThread.setName("Emulator");
        emulationThread.start();
    }

    public void handleStopRequest() {
        runtime.stop();
        stopUiLoop();
    }

    public void provideInput(int value) {
        context.provideInput(value);
    }

    private void startUiLoop() {
        uiScheduler.scheduleAtFixedRate(() -> {
            ExecutionSnapshot snapshot = context.snapshotAndClearDirty();

            Platform.runLater(() -> {
                bridge.transmit().updateFromSnapshot(snapshot);
            });
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void stopUiLoop() {
        uiScheduler.shutdownNow();
    }
}
