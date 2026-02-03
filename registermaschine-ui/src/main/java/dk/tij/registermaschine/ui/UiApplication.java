package dk.tij.registermaschine.ui;

import dk.tij.registermaschine.core.runtime.BasicExecutionContext;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.ui.config.ConfigParser;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.util.Objects;

public class UiApplication extends Application {
    private Stage primaryStage;
    private WebEngine webEngine;
    private JSObject window;

    private final InstructionSet instructionSet;
    private final BasicExecutionContext cpu;
    
    public UiApplication() {
        this.instructionSet = new InstructionSet();
        try {
            CoreConfigParser.parseConfig(instructionSet, new ConfigParser());
        } catch (Exception _) {}
        this.cpu = new BasicExecutionContext();
    }

    public static void externalLaunch(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        Scene scene = new Scene(createWebView());
        stage.setScene(scene);
        stage.setTitle("JASM v2.0.0 - by @TiJ");
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.show();
    }

    private WebView createWebView() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.load(Objects.requireNonNull(getClass().getResource("/ide.html")).toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((_, _, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                onWebViewLoaded();
            }
        });

        webView.setContextMenuEnabled(false);

        return webView;
    }

    private void onWebViewLoaded() {
        window = (JSObject) webEngine.executeScript("window");

        window.call("initializeRegisters", cpu.getRegisterCount());

        Object[] docs = instructionSet.getInstructions().stream()
                .map(i -> {
                    // Wrap in a standard HashMap so the bridge can access .get()
                    var o = new java.util.HashMap<String, String>();
                    o.put("name", i.mnemonic());
                    o.put("description", i.description());
                    return o;
                })
                .toArray();

        window.call("initializeDocs", (Object) docs);

        window.call("initializeRegisters", CoreConfig.REGISTERS);
    }
}
