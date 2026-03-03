package dk.tij.registermaschine.ui.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileHandler {
    public static final Path DEFAULT_PATH = Path.of(System.getProperty("user.home"), "jasm");
    public static final String JASM_FILE_EXTENSION = ".jasm";

    private final Stage primaryStage;
    private final FileChooser fileChooser;

    private File currentFile = null;

    public FileHandler(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.fileChooser = new FileChooser();

        try {
            if (!Files.exists(DEFAULT_PATH))
                Files.createDirectories(DEFAULT_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileChooser.setInitialDirectory(DEFAULT_PATH.toFile());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java ASM Files", "*" + JASM_FILE_EXTENSION)
        );
    }

    public void createNew() {
        currentFile = null;
    }

    public Optional<String> loadFile() {
        fileChooser.setTitle("Open JASM File");
        File selected = fileChooser.showOpenDialog(primaryStage);

        if (selected != null) {
            currentFile = selected;

            try {
                return Optional.of(Files.readString(selected.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public void save(String content) {
        if (currentFile == null) {
            saveAs(content);
        } else {
            try {
                writeFile(currentFile, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAs(String content) {
        fileChooser.setTitle("Save JASM File");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            if (!file.getName().endsWith(JASM_FILE_EXTENSION)) {
                file = new File(file.getAbsolutePath() + JASM_FILE_EXTENSION);
            }

            try {
                writeFile(file, content);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentFile = file;
        }
    }

    public String getCurrentFileName() {
        return (currentFile != null) ? currentFile.getName() : "Untitled";
    }

    private void writeFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }
}
