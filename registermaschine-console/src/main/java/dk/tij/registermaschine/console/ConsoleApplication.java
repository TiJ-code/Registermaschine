package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.compilation.CompiledProgram;
import dk.tij.registermaschine.core.cpu.BasicExecutionContext;
import dk.tij.registermaschine.core.compilation.TokenCollection;
import dk.tij.registermaschine.core.compilation.AbstractSyntaxTree;
import dk.tij.registermaschine.core.compilation.internal.Compiler;
import dk.tij.registermaschine.core.runtime.Executor;
import dk.tij.registermaschine.core.config.ConfigParser;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.core.compilation.compiling.CompiledInstruction;
import dk.tij.registermaschine.core.compilation.internal.Lexer;
import dk.tij.registermaschine.core.compilation.internal.Parser;
import dk.tij.registermaschine.core.runtime.Pipeline;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class ConsoleApplication {
    static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }

        InstructionSet registry = initRegistry();
        CompiledProgram program = new CompiledProgram();

        if (args[0].equalsIgnoreCase("-i") && args.length == 1) {
            runInteractiveMode(registry);
            return;
        }

        if (args[0].equalsIgnoreCase("-r") && args.length >= 2) {
            loadBinary(args[1], program);
            BasicExecutionContext cpu = new BasicExecutionContext();
            cpu.addListener(new MachineListener(null));
            new Executor(cpu, registry, program).run();
            return;
        }

        String sourcePath = args[0];
        String source = Files.readString(Path.of(sourcePath), StandardCharsets.UTF_8);

        var tokens = runLexer(source, false);
        if (hasFlag(args, "-t")) {
            String target = getArgAfter(args, "-t");
            saveTextFile(target, tokens);
            return;
        }

        var ast = runParser(tokens, false);
        if (hasFlag(args, "-a")) {
            String target = getArgAfter(args, "-a");
            saveTextFile(target, ast);
            return;
        }

        program = Compiler.compile(ast, registry);

        if (hasFlag(args, "-o")) {
            String outputPath = getArgAfter(args, "-o");
            saveBinary(outputPath, program);
        }

        if (hasFlag(args, "-r")) {
            BasicExecutionContext cpu = new BasicExecutionContext();
            cpu.addListener(new MachineListener());
            new Executor(cpu, registry, program).run();
        }
    }

    static void runInteractiveMode(InstructionSet registry) {
        Scanner scanner = new Scanner(System.in);

        BasicExecutionContext cpu = new BasicExecutionContext();
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
                CompiledProgram singleStep = Pipeline.tokenize(line).parse().compile(registry);

                exec.setProgram(singleStep);
                cpu.setProgrammeCounter(0);
                exec.run();

                if (cpu.isHalted()) {
                    System.out.println("CPU is halted. Terminating...");
                    break;
                }
            } catch (Exception e) {
                System.err.println("Syntax Error: " + e.getMessage());
            }
        }
    }

    static String getArgAfter(String[] args, String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(flag) &&  i + 1 < args.length) {
                return args[i + 1];
            }
        }
        throw new RuntimeException("Missing argument for flag " + flag);
    }

    static void saveTextFile(String path, List<?> items) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) sb.append(item.toString()).append("\n");
        Files.writeString(Path.of(path), sb.toString());
        System.out.println("Output saved to: " + path);
    }

    static void printUsage() {
        System.out.printf("%s %s%n", "Usage:", "./core ");
        System.out.printf("  %-30s%s%n", "<src.jasm> -o <out.o>", ": Compile source to binary");
        System.out.printf("  %-30s%s%n", "<src.jasm> -o <out.o> -r", ": Compile and run");
        System.out.printf("  %-30s%s%n", "-r <out.o>", ": Run binary file");
        System.out.printf("  %-30s%s%n", "<src.jasm> -t <out.txt>", ": Dump tokens");
        System.out.printf("  %-30s%s%n", "<src.jasm> -a <out.txt>", ": Dump Abstract Syntax Tree");
        System.out.printf("  %-30s%s%n", "-i", ": Run as console text program");
    }

    static void loadBinary(String fileName, List<CompiledInstruction> program) throws Exception {
        if (fileName == null) {
            System.err.println("Error: No input file specified for -r");
            return;
        }

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(Path.of(fileName)))) {
            dis.readInt(); // consume magic number

            int instructions = dis.readInt();
            for (int i = 0; i < instructions; i++) {
                byte opcode = dis.readByte();
                byte operandCount = dis.readByte();

                int[] operands = new int[operandCount];
                for (byte j = 0; j < operandCount; j++) {
                    operands[j] = dis.readInt();
                }

                program.add(new CompiledInstruction(opcode, operands));
            }
        }
    }

    static void saveBinary(String fileName, List<CompiledInstruction> program) throws Exception {
        if (fileName == null) {
            System.err.println("Error: No output file specified for -o");
            return;
        }

        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Path.of(fileName)))) {
            dos.writeInt(0x4A_41_53_4D);

            dos.writeInt(program.size());
            for (var instruction : program) {
                dos.writeByte(instruction.opcode());
                dos.writeByte(instruction.operands().length);

                for (int operand : instruction.operands()) {
                    dos.writeInt(operand);
                }
            }
        }

        System.out.println("binary successfully compiled to: " + fileName);
    }

    static TokenCollection runLexer(String source, boolean dump) {
        TokenCollection tokens = Lexer.tokenize(source);
        if (dump)
            tokens.forEach(System.out::println);
        return tokens;
    }

    static AbstractSyntaxTree runParser(TokenCollection tokens, boolean dump) {
        AbstractSyntaxTree ast = Parser.parse(tokens);
        if (dump)
            ast.forEach(System.out::println);
        return ast;
    }

    static InstructionSet initRegistry() {
        InstructionSet registry = new InstructionSet();
        try (InputStream is = ConsoleApplication.class.getClassLoader().getResourceAsStream("configuration.jxml")) {
            if (is == null)
                throw new RuntimeException("Could not find configuration file: configuration.jxml");
            ConfigParser.parseConfig(registry, is, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return registry;
    }

    static boolean hasFlag(String[] args, String flag) {
        for (String arg : args)
            if (arg.equalsIgnoreCase(flag)) return true;
        return false;
    }

    static String loadSource(String path) throws Exception {
        try (InputStream is = ConsoleApplication.class.getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
