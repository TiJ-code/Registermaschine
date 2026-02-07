package dk.tij.registermaschine.ui;

import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.ui.listeners.InstructionParserListener;
import dk.tij.registermaschine.ui.ui.JavaScriptBridge;
import dk.tij.registermaschine.ui.utils.InstructionMapper;
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

    private JavaScriptBridge jsBridge;
    private final IInstructionSet instructionSet;
    
    public UiApplication() {
        CoreConfigParser.init();
        CoreConfigParser.addListenerToTarget(CoreConfigParser.PARSER_INSTRUCTIONS, new InstructionParserListener());
        this.instructionSet = new ConcreteInstructionSet();
        CoreConfigParser.parseDefaultInstructionSet(instructionSet);
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
        jsBridge = new JavaScriptBridge((JSObject) webEngine.executeScript("window"));

        jsBridge.initialiseRegisters(CoreConfig.REGISTERS);

        var docs = InstructionMapper.toDocList(instructionSet);
        jsBridge.initialiseDocumentation(docs);

        var keywords = InstructionMapper.toKeywords(instructionSet);
        jsBridge.initialiseKeywords(keywords);
    }
}
