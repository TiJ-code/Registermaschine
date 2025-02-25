package dk.tij.registermaschine.handler;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class FileHandler {
    public static final Path ROOT_PATH = Path.of(System.getProperty("user.home"), "jasm");
    public static Path selectedFilePath = ROOT_PATH.resolve("jasm");

    // Create the root directory if it doesn't exist
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

    // Create a new file with an incremented name
    public static void createNewFile() {
        String fileName = findLatestNewFileName();
        Path filePath = ROOT_PATH.resolve(fileName + ".jasm");
        try {
            Files.createFile(filePath);
            System.out.println("Created new file: " + filePath);
        } catch (FileAlreadyExistsException e) {
            System.err.println("File already exists: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to create new file: " + e.getMessage());
        }
    }

    // Find the latest new file name based on existing files
    private static String findLatestNewFileName() {
        try (Stream<Path> paths = Files.list(ROOT_PATH)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith("new_file_"))
                    .map(name -> name.substring("new_file_".length(), name.lastIndexOf('.')))
                    .map(Integer::parseInt)
                    .max(Integer::compareTo)
                    .map(maxIndex -> "new_file_" + (maxIndex + 1))
                    .orElse("new_file_0");
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            return "new_file_0";
        }
    }

    public static void saveFile(Path filePath, String content) {
        try {
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File saved: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static String readFile(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return "";
        }
    }

    public static List<String> listFiles() {
        try (Stream<Path> paths = Files.list(ROOT_PATH)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            System.err.println("Error listing files: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}