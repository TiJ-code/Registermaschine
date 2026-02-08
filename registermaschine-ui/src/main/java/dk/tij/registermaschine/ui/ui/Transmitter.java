package dk.tij.registermaschine.ui.ui;

import dk.tij.registermaschine.core.runtime.ExecutionSnapshot;
import javafx.application.Platform;
import netscape.javascript.JSObject;

import java.util.List;
import java.util.Map;

public final class Transmitter {
    private final JSObject window;

    public Transmitter(JSObject window) {
        this.window = window;
    }

    public void initialiseRegisters(int regCount) {
        Platform.runLater(() -> window.call("initialiseRegisters", regCount));
    }

    public void initialiseDocumentation(List<?> docs) {
        Platform.runLater(() -> window.call("initialiseDocs", (Object) docs.toArray()));
    }

    public void initialiseKeywords(List<String> keywords) {
        Platform.runLater(() -> window.call("initialiseKeywords", (Object) keywords.toArray()));
    }

    public void updateRegister(int idx, int value) {
        Platform.runLater(() -> window.call("updateRegister", idx, value));
    }

    public void updateOutput(int value) {
        Platform.runLater(() -> window.call("updateOutput", value));
    }

    public void updateFromSnapshot(ExecutionSnapshot snapshot) {
        Map<Integer, Integer> registers = snapshot.registers();
        for (Map.Entry<Integer, Integer> entry : registers.entrySet()) {
            updateRegister(entry.getKey(), entry.getValue());
        }

        if (snapshot.output() != null)
            updateOutput(snapshot.output());
    }

    public void requestInput() {
        Platform.runLater(() -> window.call("onInputRequested"));
    }

    public void notifyProgramFinished() {
        System.out.println("nofication");
        Platform.runLater(() -> window.call("programFinished"));
    }
}
