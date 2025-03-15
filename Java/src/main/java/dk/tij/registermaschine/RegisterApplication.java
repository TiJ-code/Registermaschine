package dk.tij.registermaschine;

import dk.tij.registermaschine.editor.SyntaxHighlighter;
import dk.tij.registermaschine.handler.FileHandler;
import dk.tij.registermaschine.parser.Instruction;
import dk.tij.registermaschine.parser.InstructionParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RegisterApplication extends Application {
    public static String CODE = "";
    private static String LOADED_CODE;
    private static volatile boolean loadingFile = false;
    private static volatile boolean unsaved = false;

    private CPU cpu;
    private FileChooser fileChooser;

    private Stage primaryStage;
    private CodeArea codeArea;
    private WebEngine ideWebEngine;
    private JSObject window;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open JASM File");
        fileChooser.setInitialDirectory(FileHandler.ROOT_PATH.toFile());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java ASM Files", "*.jasm")
        );
        FileHandler.createRegisterDirectories();

        Scene scene = new Scene(createLayout(), 1100, 970);
        stage.setScene(scene);
        stage.setTitle("JASM v1.4.8 - By @TiJ --- Special Thanks: @Michael @Janek @Steven --- [Java 11]");
        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(970);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        stage.show();
    }

    private SplitPane createLayout() {
        SplitPane splitPane = new SplitPane(createCodeArea(), createRegisterView());
        splitPane.setDividerPositions(0.5);
        return splitPane;
    }

    private CodeArea createCodeArea() {
        codeArea = new CodeArea();
        codeArea.setLineHighlighterOn(true);
        codeArea.setLineHighlighterFill(Color.rgb(70, 70, 70));
        codeArea.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/dark-mode.css")).toExternalForm());
        codeArea.getStyleClass().add("code-area");
        codeArea.setParagraphGraphicFactory(line -> {
            String hexLineNumber = String.format("0x%02X", line + 1);
            Text lineNumberText = new Text(hexLineNumber);

            lineNumberText.getStyleClass().add("lineno");
            lineNumberText.setFill(Color.WHITE);
            lineNumberText.setFont(Font.font("Monospace", 16));
            lineNumberText.setStyle("-fx-padding: 5px 0 5px 0;");

            // Create a VBox to center the text vertically
            VBox vbox = new VBox(lineNumberText);
            vbox.setAlignment(Pos.CENTER_LEFT); // Align to the left
            vbox.setPrefHeight(30); // Set a preferred height for the VBox

            return vbox;
        });

        SyntaxHighlighter.applyHighlighting(codeArea);
        codeArea.textProperty().addListener((obs, oldValue, newValue) -> {
            CODE = newValue;
            if (loadingFile) return;
            String functionToCall;
            if (!CODE.equals(LOADED_CODE)) {
                functionToCall = "markLoadedFileAsEdited";
                unsaved = true;
            } else {
                functionToCall = "markLoadedFileAsUnedited";
                unsaved = false;
            }
            Platform.runLater(() -> window.call(functionToCall));
        });

        codeArea.setMinWidth(200);

        return codeArea;
    }

    private WebView createRegisterView() {
        WebView webView = new WebView();
        ideWebEngine = webView.getEngine();

        ideWebEngine.load(Objects.requireNonNull(getClass().getResource("/html/ide.html")).toExternalForm());

        ideWebEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                window = (JSObject) ideWebEngine.executeScript("window");
                window.setMember("java", this);
                cpu = new CPU(window, codeArea);
            }
        });

        webView.setContextMenuEnabled(false);
        webView.setMinWidth(880);

        return webView;
    }

    // js call
    public void runCode(float speed) {
        List<Instruction> instructions;
        try {
            instructions = InstructionParser.parse(CODE);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing code: " + e.getMessage());
            return;
        }

        codeArea.setLineHighlighterOn(false);
        cpu.executeCode(instructions, speed);
    }

    // js call
    public void endExecution() {
        cpu.endExecution();
    }

    // js call
    public void sendInput(String inputValue) {
        cpu.sendInput(inputValue);
    }

    // js call
    public void setDebugMode(boolean value) {
        cpu.toggleDebugMode(value);
    }

    // js call
    public void loadFile() {
        loadFile(fileChooser.showOpenDialog(primaryStage));
    }

    private void loadFile(File loadedFile) {
        Platform.runLater(() -> window.call("displayLoadedFile", loadedFile.getName()));
        try {
            loadingFile = true;
            String newCode = FileHandler.readFile(loadedFile);
            codeArea.replaceText(newCode);
            codeArea.appendText("\n");
            codeArea.replaceText(newCode);
            loadingFile = false;
            LOADED_CODE = CODE = newCode;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    // js call
    public void saveFile() {
        File savedFile;
        if (FileHandler.currentWorkingFile != null) {
            savedFile = FileHandler.currentWorkingFile;
        } else {
            savedFile = fileChooser.showSaveDialog(primaryStage);
        }
        storeFile(savedFile);
    }

    // js call
    public void saveAsFile() {
        storeFile(fileChooser.showSaveDialog(primaryStage));
    }

    public void newDocument() {
        if (unsaved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("New Document");
            alert.setHeaderText("You have unsaved changes!");
            alert.setContentText("Do you want to save this file?\nYes = Saves file and creates new document\nNo = Does not save the current file and creates new document\nCancel = Aborts the creation of a new document.");

            ButtonType saveFileButton = new ButtonType("Yes");
            ButtonType doNotSaveButton = new ButtonType("No");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveFileButton, doNotSaveButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty()) {
                System.err.println("No button pressed but still continued /shrug");
                return;
            }
            if (result.get() == saveFileButton) {
                saveFile();
                performCreationOfNewDocument();
            } else if (result.get() == cancelButton) {
                return;
            }

            performCreationOfNewDocument();
        }
    }

    private void performCreationOfNewDocument() {
        LOADED_CODE = ".";
        CODE = "";
        codeArea.replaceText(CODE);
        Platform.runLater(() -> window.call("displayLoadedFile", "unsaved_file.jasm"));
    }

    private void storeFile(File savedFile) {
        try {
            FileHandler.saveFile(savedFile, CODE);
            loadFile(savedFile);
            unsaved = false;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
