package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.compilation.Pipeline;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompilerService {
    private final IInstructionSet registry;

    public CompilerService(IInstructionSet registry) {
        this.registry = registry;
    }

    public void process(CliOptions options) throws Exception {
        // 1) Interactive
        if (options.interactive()) {
            ConsoleApplication.runInteractiveMode(registry);
            return;
        }

        ICompiledProgram program;
        Path srcPath = Path.of(options.sourcePath());

        // 2) Run existing binary
        if (options.sourcePath().endsWith(".o") || options.sourcePath().endsWith(".bin")) {
            program = BinarySerializer.load(srcPath);
        } else {
        // 3) Compile from source
            String source = Files.readString(srcPath, StandardCharsets.UTF_8);

            // Dump Tokens
            var tokenStage = Pipeline.tokenize(source, registry);
            if (options.dumpTokensPath() != null)
                ConsoleApplication.saveTextFile(options.dumpTokensPath(), tokenStage.tokens());

            // Dump Syntax Tree
            var syntaxTreeStage = tokenStage.parse();
            if (options.dumpSyntaxTreePath() != null)
                ConsoleApplication.saveTextFile(options.dumpSyntaxTreePath(), syntaxTreeStage.syntaxTree());

            program = syntaxTreeStage.compile();

            if (options.outputPath() != null) {
                BinarySerializer.save(Path.of(options.outputPath()), program);
            }
        }

        if (options.shouldRun())
            ConsoleApplication.runProgram(program, registry);
    }
}
