package dk.tij.registermaschine.ui.ui;

import dk.tij.registermaschine.core.runtime.ExecutionSnapshot;
import dk.tij.registermaschine.ui.annotations.ToJava;
import dk.tij.registermaschine.ui.annotations.ToUi;
import javafx.application.Platform;
import netscape.javascript.JSObject;

import java.util.List;
import java.util.Map;

public final class Transmitter {
    private final JSObject window;

    public Transmitter(JSObject window) {
        this.window = window;
    }

    @ToUi
    public void initialiseRegisters(int regCount) {
        Platform.runLater(() -> window.call("initialiseRegisters", regCount));
    }

    @ToUi
    public void initialiseDocumentation(List<?> docs) {
        Platform.runLater(() -> window.call("initialiseDocs", (Object) docs.toArray()));
    }

    @ToUi
    public void initialiseKeywords(List<String> keywords) {
        Platform.runLater(() -> window.call("initialiseKeywords", (Object) keywords.toArray()));
    }

    @ToUi
    public void updateRegister(int idx, int value) {
        Platform.runLater(() -> window.call("updateRegister", idx, value));
    }

    @ToUi
    public void updateOutput(int value) {
        Platform.runLater(() -> window.call("updateOutput", value));
    }

    @ToUi
    public void updateFromSnapshot(ExecutionSnapshot snapshot) {
        Map<Integer, Integer> registers = snapshot.registers();
        for (Map.Entry<Integer, Integer> entry : registers.entrySet()) {
            updateRegister(entry.getKey(), entry.getValue());
        }

        if (snapshot.output() != null)
            updateOutput(snapshot.output());
    }

    @ToUi
    public void requestInput() {
        Platform.runLater(() -> window.call("onInputRequested"));
    }

    @ToUi
    public void notifyProgramFinished() {
        Platform.runLater(() -> window.call("programFinished"));
    }

    @ToUi
    public void sendLoadedCode(String loadedCode) {
        Platform.runLater(() -> window.call("loadCode", loadedCode));
    }

    @ToUi
    public void setFileName(String fileName) {
        Platform.runLater(() -> window.call("setFileName", fileName));
    }

    @ToUi
    public void confirmFileAction() {
        Platform.runLater(() -> window.call("onFileActionConfirmed"));
    }

    @ToUi
    public void toggleBugButton(boolean active) {
        Platform.runLater(() -> window.call("toggleBugButton", active));
    }

    @ToUi
    public void closeBugModal() {
        Platform.runLater(() -> window.call("toggleModal", "bug-modal", false));
    }
}
