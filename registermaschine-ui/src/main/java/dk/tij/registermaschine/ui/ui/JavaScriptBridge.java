package dk.tij.registermaschine.ui.ui;

import netscape.javascript.JSObject;

import java.util.List;

public class JavaScriptBridge {
    private final JSObject window;

    public JavaScriptBridge(JSObject object) {
        this.window = object;
        this.window.setMember("java", this);
    }

    public void println(String text) {
        System.out.printf("[JS]: %s%n", text);
    }

    public void sendSourceCode(String sourceCode) {
        println(sourceCode);
    }

    public void runProgram() {
        println("running");
    }

    public void saveProgram() {

    }

    public void initialiseRegisters(int regCount) {
        window.call("initialiseRegisters", regCount);
    }

    public void initialiseDocumentation(List<?> docs) {
        window.call("initialiseDocs", (Object) docs.toArray());
    }

    public void initialiseKeywords(List<String> keywords) {
        window.call("initialiseKeywords", (Object) keywords.toArray());
    }
}
