package dk.tij.registermaschine.console;

public record CliOptions(String sourcePath, String outputPath, boolean shouldRun, boolean interactive,
                         String dumpTokensPath, String dumpSyntaxTreePath) {
    public static CliOptions parse(String[] args) {
        if (args[0].equalsIgnoreCase("-i") && args.length == 1)
            return new CliOptions(null, null, false, true, null, null);

        if (args[0].equalsIgnoreCase("-r") && args.length >= 2)
            return new CliOptions(args[1], null, true, false, null, null);

        String tokensPath = null, syntaxTreePath = null;

        String sourcePath = args[0], outputPath = null;

        // Debug Dumps
        if (hasFlag(args, "-t")) {
            tokensPath = getArgAfter(args, "-t");
        }

        if (hasFlag(args, "-a")) {
            syntaxTreePath = getArgAfter(args, "-a");
        }

        boolean shouldSave = hasFlag(args, "-o");
        boolean shouldRun = hasFlag(args, "-r") || hasFlag(args, "-or") || !shouldSave;

        if (shouldSave) {
            outputPath = getArgAfter(args, "-o");
        }

        return new CliOptions(sourcePath, outputPath, shouldRun, false, tokensPath, syntaxTreePath);
    }

    private static String getArgAfter(String[] args, String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(flag) &&  i + 1 < args.length) {
                return args[i + 1];
            }
        }
        throw new RuntimeException("Missing argument for flag " + flag);
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args)
            if (arg.equalsIgnoreCase(flag)) return true;
        return false;
    }
}
