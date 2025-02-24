package dk.tij.registermaschine;

import dk.tij.registermaschine.editor.SyntaxHighlighter;
import dk.tij.registermaschine.parser.Instruction;
import dk.tij.registermaschine.parser.InstructionLookupTable;
import dk.tij.registermaschine.parser.InstructionParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.Objects;

public class RegisterApplication extends Application {
    private WebEngine webEngine;
    private CodeArea codeArea;
    private CPU cpu;
    private JSObject window;

    public static String CODE = "";

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createLayout(), 1100, 970);
        stage.setScene(scene);
        stage.setTitle("JASM v1.3.3 - By @TiJ - Special Thanks: @Michael @Janek @Steven");
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
        codeArea.textProperty().addListener((o, oV, newValue) -> {
            CODE = newValue;
        });

        codeArea.setMinWidth(200);

        return codeArea;
    }

    private WebView createRegisterView() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.load(Objects.requireNonNull(getClass().getResource("/html/index.html")).toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((_, _, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", this);
                cpu = new CPU(window, codeArea);
            }
        });

        webView.setContextMenuEnabled(false);
        webView.setMinWidth(880);

        return webView;
    }

    private void displayMachineCode(List<Instruction> instructions) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Instruction instruction : instructions) {
            stringBuilder.append(String.format("%02X ", instruction.opcode()));
            if (instruction.opcode() == InstructionLookupTable.opcodesPerInstruction.get("HLT")) {
                stringBuilder.append(String.format("%d", instruction.argument()));
            } else {
                stringBuilder.append(String.format("%02X", instruction.argument()));
            }
            stringBuilder.append("<br>");
        }
        Platform.runLater(() -> {
            if (stringBuilder.isEmpty()) return;
            window.call("displayMachineCode", stringBuilder.toString());
        });
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

        displayMachineCode(instructions);

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

    public static void main(String[] args) {
        launch(args);
    }
}
