package dk.tij.registermaschine.console;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public record CliOptions(String sourcePath, String outputPath, boolean shouldRun, boolean interactive,
                         String dumpTokensPath, String dumpSyntaxTreePath) {
    public static String CALLER = "java -jar console*.jar";
    private static final Options OPTIONS = createOptions();

    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder()
                .longOpt("source")
                .hasArg()
                .argName("src.jasm")
                .desc("Source file")
                .get()
        );

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("out.o")
                .desc("Output binary file")
                .get()
        );

        options.addOption(Option.builder("r")
                .longOpt("run")
                .desc("Run after compile or run binary")
                .get()
        );

        options.addOption(Option.builder("or")
                .longOpt("output-run")
                .desc("Compile program and run")
                .get()
        );

        options.addOption(Option.builder("t")
                .longOpt("tokens")
                .hasArg()
                .argName("out.txt")
                .desc("Dump tokens")
                .get()
        );

        options.addOption(Option.builder("a")
                .longOpt("ast")
                .hasArg()
                .argName("out.txt")
                .desc("Dump AST")
                .get()
        );

        options.addOption(Option.builder("i")
                .longOpt("interactive")
                .desc("Interactive mode")
                .get()
        );

        return options;
    }

    public static CliOptions parse(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments provided\n" + getUsage());
        }

        String[] replacementArgs = null;
        final String callerPrefix = "name=";
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(callerPrefix)) {
                CALLER = args[i].substring(callerPrefix.length());
                replacementArgs = new String[args.length - 1];
                System.arraycopy(args, 0, replacementArgs, 0, i);
                System.arraycopy(args, i + 1, replacementArgs, i, args.length - i - 1);
                break;
            }
        }
        if (replacementArgs != null)
            args = replacementArgs;

        CommandLineParser parser = new DefaultParser();

        try {
            if (args.length == 1 && args[0].equalsIgnoreCase("-i")) {
                return new CliOptions(null, null, false, true, null, null);
            }

            CommandLine cmd = parser.parse(OPTIONS, args);

            boolean interactive = cmd.hasOption("i");
            boolean compileRun = cmd.hasOption("or");
            boolean runFlag = cmd.hasOption("r");

            String output = cmd.getOptionValue("o");
            String tokens = cmd.getOptionValue("t");
            String ast = cmd.getOptionValue("a");

            String positional = cmd.getArgList().isEmpty() ? null : cmd.getArgList().getFirst();

            if (interactive) {
                if (args.length != 1)
                    throw new ParseException("-i must be standalone");
                return new CliOptions(null,null,false,true,null,null);
            }

            if (runFlag && output == null && tokens == null && ast == null && positional != null && !positional.endsWith(".jasm")) {

                if (!positional.endsWith(".o") && !positional.endsWith(".bin"))
                    throw new ParseException("Binary must end with .o or .bin");

                return new CliOptions(positional, null, true, false, null, null);
            }

            String source = positional;

            if (compileRun) {
                if (source == null || !source.endsWith(".jasm"))
                    throw new ParseException("-or requires <src.jasm>");

                return new CliOptions(source, null, true, false, tokens, ast);
            }

            if (source != null && source.endsWith(".jasm")) {

                if (output == null && tokens == null && ast == null)
                    throw new ParseException("Nothing to do: specify -o, -t, -a, or -r");

                return new CliOptions(source, output, runFlag, false, tokens, ast);
            }

            throw new ParseException("Invalid command");
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage() + "\n" + getUsage(), e);
        }

    }

    public static String getUsage() {
        StringBuilder sb = new StringBuilder();

        sb.append("%s ./%s %n".formatted("Usage:", CALLER));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -o <out.o>", ": Compile source to binary"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -o <out.o> -r", ": Compile and run"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -or", ": Compile and run"));
        sb.append("  %-30s%s%n".formatted("<out.o> -r", ": Run binary file"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -t <out.txt>", ": Dump syntaxTree"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -a <out.txt>", ": Dump Abstract Syntax Tree"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -o <out.o> -t <t.txt> -a <a.txt>", ": Combine"));
        sb.append("  %-30s%s%n".formatted("<src.jasm> -o <out.o> -t <t.txt> -a <a.txt> -r", ": Combine & run"));
        sb.append("  %-30s%s".formatted("-i", ": Run as console text program"));

        return sb.toString();
    }
}
