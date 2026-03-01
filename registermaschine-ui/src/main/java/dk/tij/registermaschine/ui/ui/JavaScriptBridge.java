package dk.tij.registermaschine.ui.ui;

import dk.tij.registermaschine.ui.SimulationController;
import dk.tij.registermaschine.ui.annotations.ToJava;
import dk.tij.registermaschine.ui.annotations.ToUi;
import dk.tij.registermaschine.ui.utils.FileHandler;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.Optional;

public class JavaScriptBridge {
    private final Transmitter transmitter;
    private SimulationController controller;
    private FileHandler fileHandler;

    public JavaScriptBridge(JSObject object) {
        object.setMember("java", this);
        this.transmitter = new Transmitter(object);
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
    }

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        transmit().setFileName(fileHandler.getCurrentFileName());
    }

    @ToJava
    public void println(String text) {
        System.out.printf("[JS]: %s%n", text);
    }

    @ToJava
    public void runProgram(String sourceCode, boolean useDebug) {
        System.out.println(sourceCode);
        if (controller != null) {
            controller.handleRunRequest(sourceCode, useDebug);
        }
    }

    @ToJava
    public void stopProgram() {
        controller.handleStopRequest();
    }

    @ToJava
    public void provideInput(int value) {
        if (controller != null) {
            controller.provideInput(value);
        }
    }

    @ToJava
    public void saveFile(String content) {
        if (fileHandler != null) {
            fileHandler.save(content);
            transmit().setFileName(fileHandler.getCurrentFileName());
        }
    }

    @ToJava
    public void saveAsFile(String content) {
        if (fileHandler != null) {
            fileHandler.saveAs(content);
            transmit().setFileName(fileHandler.getCurrentFileName());
        }
    }

    @ToJava
    public void loadFile() {
        if (fileHandler != null) {
            System.out.println("trying to load");
            Optional<String> optCode = fileHandler.loadFile();
            optCode.ifPresent(code -> {
                transmit().sendLoadedCode(code);
                transmit().setFileName(fileHandler.getCurrentFileName());
            });
        }
    }

    @ToJava
    public void newDocument(String content) {
        if (fileHandler != null) {
            if (content != null && !content.isBlank())
                fileHandler.save(content);
            fileHandler.createNew();
            transmit().setFileName(fileHandler.getCurrentFileName());
        }
    }

    @ToUi
    public Transmitter transmit() {
        return transmitter;
    }
}
