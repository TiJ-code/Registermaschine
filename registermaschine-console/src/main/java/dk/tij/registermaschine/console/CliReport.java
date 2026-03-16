package dk.tij.registermaschine.console;

import java.util.Scanner;

public final class CliReport {
    private CliReport() {}

    public static Result interactiveReport() {
        Scanner input = new Scanner(System.in);

        System.out.print("Title: ");
        String title = input.nextLine();
        System.out.println("Description:");
        System.out.println("(Type 'END' on a new line, to end input):");
        String description = readMultiLineFromConsole(input);

        input.close();

        return new Result(title, description);
    }

    private static String readMultiLineFromConsole(Scanner input) {
        StringBuilder sb = new StringBuilder();

        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (line.equalsIgnoreCase("END")) break;
            sb.append(line).append("\n");
        }

        return sb.toString().trim();
    }

    public record Result(String title, String description) { }
}
