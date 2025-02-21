package dk.tij.registermaschine;

import dk.tij.registermaschine.editor.SyntaxHighlighter;
import dk.tij.registermaschine.parser.Instruction;
import dk.tij.registermaschine.parser.InstructionLookupTable;
import dk.tij.registermaschine.parser.InstructionParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
        Scene scene = new Scene(createLayout(), 1280, 900);
        stage.setScene(scene);
        stage.setTitle("JASM v1.2.0 - By @TiJ - Credits: @Steven @Michael @Janek");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        stage.show();
    }

    private SplitPane createLayout() {
        SplitPane splitPane = new SplitPane(createCodeArea(), createRegisterView());

        double dividerPosition = (1280d - 610d) / 1280d;

        splitPane.setMaxHeight(900);
        splitPane.setPrefHeight(900);
        splitPane.setMaxWidth(1280);
        splitPane.setPrefWidth(1280);
        splitPane.setDividerPositions(dividerPosition);
        SplitPane.Divider divider = splitPane.getDividers().getFirst();
        divider.positionProperty().addListener((_, _, _) -> {
            divider.setPosition(dividerPosition);
        });

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

            return lineNumberText;
        });

        SyntaxHighlighter.applyHighlighting(codeArea);
        codeArea.textProperty().addListener((o, oV, newValue) -> {
            CODE = newValue;
        });

        return codeArea;
    }

    private WebView createRegisterView() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.load(Objects.requireNonNull(getClass().getResource("/index.html")).toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", this);
                cpu = new CPU(window, codeArea);
            }
        });

        webView.setContextMenuEnabled(false);
        webView.maxWidth(300);
        webView.setPrefWidth(300);

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
            window.call("displayMachineCode", stringBuilder.toString());
        });
    }

    // js call
    public void runCode() {
        List<Instruction> instructions;
        try {
            instructions = InstructionParser.parse(CODE);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing code: " + e.getMessage());
            return;
        }

        displayMachineCode(instructions);

        codeArea.setLineHighlighterOn(false);
        cpu.executeCode(instructions);
    }

    // js call
    public void endExecution() {
        cpu.endExecution();
    }

    // js call
    public void sendInput(String inputValue) {
        cpu.sendInput(inputValue);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
