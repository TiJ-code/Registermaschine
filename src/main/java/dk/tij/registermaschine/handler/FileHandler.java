package dk.tij.registermaschine.handler;

import java.io.*;
import java.nio.file.*;

public final class FileHandler {
    public static final Path ROOT_PATH = Path.of(System.getProperty("user.home"), "jasm");
    public static File currentWorkingFile;

    public static void createRegisterDirectories() {
        try {
            if (!Files.exists(ROOT_PATH)) {
                Files.createDirectories(ROOT_PATH);
                System.out.println("Created directory: " + ROOT_PATH);
            } else {
                System.out.println("Directory already exists: " + ROOT_PATH);
            }
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    public static void saveFile(File toSaveFile, String content) throws IOException {
        try {
            Files.writeString(toSaveFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File saved: " + toSaveFile.toPath());
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static String readFile(File fileToLoad) throws IOException {
        currentWorkingFile = fileToLoad;
        return Files.readString(fileToLoad.toPath());
    }
}