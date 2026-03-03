package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.error.SyntaxErrorException;
import dk.tij.registermaschine.core.instructions.JumpInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.runtime.ConcreteExecutionContext;
import dk.tij.registermaschine.core.runtime.Executor;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.core.compilation.Pipeline;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ConsoleApplication {
    static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }

        try {
            CliOptions options = CliOptions.parse(args);

            CoreConfigParser.init();

            IInstructionSet registry = new ConcreteInstructionSet();
            CoreConfigParser.parseDefaultInstructionSet(registry);
            Pipeline.setGlobalInstructionSet(registry);

            CompilerService service = new CompilerService(registry);
            service.process(options);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static void runProgram(ICompiledProgram program, IInstructionSet reg) {
        Scanner in = new Scanner(System.in);
        ConcreteExecutionContext cpu = new ConcreteExecutionContext();
        cpu.addListener(new MachineListener(in));
        new Executor(cpu, reg, program).run();
        in.close();
    }

    static void runInteractiveMode(IInstructionSet registry) {
        Scanner scanner = new Scanner(System.in);

        registry.prohibitInstructionHandler(JumpInstruction.class);

        ConcreteExecutionContext cpu = new ConcreteExecutionContext();
        cpu.addListener(new MachineListener(scanner));
        Executor exec = new Executor(cpu, registry);

        System.out.println("Interactive Editor");
        System.out.println("Type your jassembly code. Type /quit to end");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("/quit")) break;

            try {
                ICompiledProgram singleStep = Pipeline.compileWithGlobal(line);

                exec.setProgram(singleStep);
                cpu.resetProgrammeCounter();
                exec.run();

                if (cpu.isHalted()) {
                    System.out.println("CPU is halted. Terminating...");
                    break;
                }
            } catch (SyntaxErrorException e) {
                System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }



    static void saveTextFile(String path, Iterable<?> items) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) sb.append(item.toString()).append("\n");
        Files.writeString(Path.of(path), sb.toString());
        System.out.println("Output saved to: " + path);
    }

    static void printUsage() {
        System.out.printf("%s %s%n", "Usage:", "./jcc ");
        System.out.printf("  %-30s%s%n", "<src.jasm> -o <out.o>", ": Compile source to binary");
        System.out.printf("  %-30s%s%n", "<src.jasm> -o <out.o> -r", ": Compile and run");
        System.out.printf("  %-30s%s%n", "-r <out.o>", ": Run binary file");
        System.out.printf("  %-30s%s%n", "<src.jasm> -t <out.txt>", ": Dump syntaxTree");
        System.out.printf("  %-30s%s%n", "<src.jasm> -a <out.txt>", ": Dump Abstract Syntax Tree");
        System.out.printf("  %-30s%s%n", "-i", ": Run as console text program");
    }

    static IInstructionSet initRegistry() {
        IInstructionSet registry = new ConcreteInstructionSet();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registry;
    }
}
