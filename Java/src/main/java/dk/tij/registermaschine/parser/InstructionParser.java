package dk.tij.registermaschine.parser;

import java.util.ArrayList;
import java.util.List;

public class InstructionParser {

    public static List<Instruction> parse(String code) throws IllegalArgumentException {
        List<Instruction> instructions = new ArrayList<Instruction>();
        String[] lines = code.split("\\r?\\n");

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 1) {
                continue;
            }

            int opcode = InstructionLookupTable.opcodesPerInstruction.getOrDefault(parts[0].toUpperCase(), 0x0A);
            int argument = 0;

            if (parts.length > 1) {
                String argumentStr = parts[1];
                try {
                    if (argumentStr.startsWith("$")) {
                        argument = Integer.parseInt(argumentStr.substring(1), 16);
                    } else if (argumentStr.startsWith("0x") || argumentStr.startsWith("0X")) {
                        argument = Integer.parseInt(argumentStr.substring(2), 16);
                    } else {
                        argument = Integer.parseInt(argumentStr);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number at line " + lineNumber);
                }
            }
            instructions.add(new Instruction(opcode, argument));
        }
        return instructions;
    }
}
