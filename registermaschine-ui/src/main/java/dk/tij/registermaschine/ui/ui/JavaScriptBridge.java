package dk.tij.registermaschine.ui.ui;

import netscape.javascript.JSObject;

import java.util.List;

public class JavaScriptBridge {
    private final JSObject window;

    public JavaScriptBridge(JSObject object) {
        this.window = object;
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
