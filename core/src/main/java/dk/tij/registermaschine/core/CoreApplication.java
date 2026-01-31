package dk.tij.registermaschine.core;

import dk.tij.registermaschine.core.config.InstructionConfigParser;
import dk.tij.registermaschine.core.config.InstructionRegistry;
import dk.tij.registermaschine.core.instructions.CompiledInstruction;
import dk.tij.registermaschine.core.parser.Lexer;
import dk.tij.registermaschine.core.parser.Parser;
import dk.tij.registermaschine.core.parser.Token;
import dk.tij.registermaschine.core.parser.ast.ASTNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CoreApplication {
    static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }

        InstructionRegistry registry = initRegistry("configuration.xml");
        List<CompiledInstruction> program = new ArrayList<>();

        if (args[0].equalsIgnoreCase("--i") && args.length == 1) {
            runInteractiveMode(registry);
            return;
        }

        if (args[0].equalsIgnoreCase("--r") && args.length >= 2) {
            loadBinary(args[1], program);
            new Executor(new CPU(), registry, program).run();
            return;
        }

        String sourcePath = args[0];
        String source = Files.readString(Path.of(sourcePath), StandardCharsets.UTF_8);

        var tokens = runLexer(source, false);
        if (hasFlag(args, "--t")) {
            String target = getArgAfter(args, "--t");
            saveTextFile(target, tokens);
            return;
        }

        var ast = runParser(tokens, false);
        if (hasFlag(args, "--a")) {
            String target = getArgAfter(args, "--a");
            saveTextFile(target, ast);
            return;
        }

        Compiler compiler = new Compiler(registry);
        program = compiler.compile(ast);

        if (hasFlag(args, "--o")) {
            String outputPath = getArgAfter(args, "--o");
            saveBinary(outputPath, program);
        }

        if (hasFlag(args, "--r")) {
            new Executor(new CPU(), registry, program).run();
        }
    }

    static void runInteractiveMode(InstructionRegistry registry) {
        Scanner scanner = new Scanner(System.in);

        CPU cpu = new CPU();
        Executor exec = new Executor(cpu, registry);

        System.out.println("Interactive Editor");
        System.out.println("Type your jassembly code. Type /quit to end");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("/quit")) break;

            try {
                var tokens = runLexer(line, false);
                var ast = runParser(tokens, false);
                var singleStep = new Compiler(registry).compile(ast);

                exec.setProgram(singleStep);
                cpu.setProgrammeCounter(0);
                exec.run();

                StringBuilder topLine = new StringBuilder();
                StringBuilder endLine = new StringBuilder();
                for (int i = 0; i < cpu.getRegisterCount(); i++) {
                    topLine.append(String.format("%4s", i == 0 ? "ACCU" : ("r" + i))).append(" ");
                    endLine.append(String.format("%4s", cpu.getRegister(i))).append(" ");
                }
                System.out.println(topLine);
                System.out.println(endLine);
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
        System.out.println("Usage:");
        System.out.println("  <src.jasm> --o <out.o>\t\t: Compile source to binary");
        System.out.println("  <src.jasm> --o <out.o> --r\t: Compile and run");
        System.out.println("  --r <out.o>\t\t\t\t\t: Run binary file");
        System.out.println("  <src.jasm> --t <out.txt>\t\t: Dump tokens");
        System.out.println("  <src.jasm> --a <out.txt>\t\t: Dump Abstract Syntax Tree");
        System.out.println("  --i\t\t\t\t\t\t\t: Run as console text program");
    }

    static void loadBinary(String fileName, List<CompiledInstruction> program) throws Exception {
        if (fileName == null) {
            System.err.println("Error: No input file specified for --r");
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
            System.err.println("Error: No output file specified for --o");
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

    static List<Token> runLexer(String source, boolean dump) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        if (dump)
            tokens.forEach(System.out::println);
        return tokens;
    }

    static List<ASTNode> runParser(List<Token> tokens, boolean dump) {
        Parser parser = new Parser(tokens);
        List<ASTNode> ast = parser.parse();
        if (dump)
            ast.forEach(System.out::println);
        return ast;
    }

    static InstructionRegistry initRegistry(String path) throws Exception {
        InstructionRegistry registry = new InstructionRegistry();
        InstructionConfigParser configParser = new InstructionConfigParser(registry);
        configParser.parseConfig(new File(CoreApplication.class.getClassLoader().getResource(path).toURI()));
        return registry;
    }

    static boolean hasFlag(String[] args, String flag) {
        for (String arg : args)
            if (arg.equalsIgnoreCase(flag)) return true;
        return false;
    }

    static String loadSource(String path) throws Exception {
        try (InputStream is = CoreApplication.class.getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
