package dk.tij.registermaschine.ui;

import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.ui.listeners.InstructionParserListener;
import dk.tij.registermaschine.ui.ui.JavaScriptBridge;
import dk.tij.registermaschine.ui.utils.InstructionMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class UiApplication extends Application {
    private static final String DEV_RESOURCES_PATH = "registermaschine-ui/src/main/resources/";
    private static boolean DEBUG_UI = false;

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
        boolean debug = System.getProperty("debug") != null;
        boolean ui = System.getProperty("ui") != null;

        if (debug && ui) {
            DEBUG_UI = true;
            System.out.println("[System] UI Debug mode activated...");
        }

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

        if (DEBUG_UI)
            startFileWatcher();

        stage.show();
    }

    private WebView createWebView() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        if (DEBUG_UI) {
            webEngine.load(Path.of(DEV_RESOURCES_PATH, "ide.html").toAbsolutePath().toUri().toString());
        } else {
            webEngine.load(Objects.requireNonNull(getClass().getResource("/ide.html")).toExternalForm());
        }

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

    private void startFileWatcher() {
        Thread watcher = new Thread(() -> {
            Path rootPath = Path.of(DEV_RESOURCES_PATH);
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                        return FileVisitResult.CONTINUE;
                    }
                });

                System.out.println("[Watcher] Monitoring recursively: " + rootPath);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changedFile = (Path) event.context();
                        String fileName = changedFile.toString();

                        if (fileName.endsWith(".html") || fileName.endsWith(".css") || fileName.endsWith(".js")) {
                            Thread.sleep(100);
                            Platform.runLater(() -> {
                                webEngine.reload();
                                System.out.println("[Watcher] Reloading IDE due to file change...");
                            });
                        }
                    }
                    if (!key.reset()) break;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Watcher stopped: " + e.getMessage());
                e.printStackTrace();
            }
        });

        watcher.setDaemon(true);
        watcher.start();
    }
}
