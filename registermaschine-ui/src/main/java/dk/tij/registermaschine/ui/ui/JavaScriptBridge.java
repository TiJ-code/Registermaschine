package dk.tij.registermaschine.ui.ui;

import dk.tij.registermaschine.ui.SimulationController;
import netscape.javascript.JSObject;

public class JavaScriptBridge {
    private final Transmitter transmitter;
    private SimulationController controller;

    public JavaScriptBridge(JSObject object) {
        object.setMember("java", this);
        this.transmitter = new Transmitter(object);
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
    }

    public void println(String text) {
        System.out.printf("[JS]: %s%n", text);
    }

    public void runProgram(String sourceCode) {
        System.out.println(sourceCode);
        if (controller != null) {
            controller.handleRunRequest(sourceCode);
            System.out.println("controller shenanigans");
        }
    }

    public void stopProgram() {
        controller.handleStopRequest();
    }

    public void provideInput(int value) {
        if (controller != null) {
            controller.provideInput(value);
        }
    }

    public Transmitter transmit() {
        return transmitter;
    }
}
