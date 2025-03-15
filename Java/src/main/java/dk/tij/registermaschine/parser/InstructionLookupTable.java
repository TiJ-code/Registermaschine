package dk.tij.registermaschine.parser;

import java.util.HashMap;
import java.util.Map;

public final class InstructionLookupTable {
    public final static Map<String, Integer> opcodesPerInstruction = new HashMap<String, Integer>();

    static {
        opcodesPerInstruction.put("ADD", 0x01);
        opcodesPerInstruction.put("SUB", 0x02);
        opcodesPerInstruction.put("MUL", 0x03);
        opcodesPerInstruction.put("DIV", 0x04);
        opcodesPerInstruction.put("LDA", 0x05);
        opcodesPerInstruction.put("LDK", 0x06);
        opcodesPerInstruction.put("STA", 0x07);
        opcodesPerInstruction.put("INP", 0x08);
        opcodesPerInstruction.put("OUT", 0x09);
        opcodesPerInstruction.put("HLT", 0x0A);
        opcodesPerInstruction.put("JMP", 0x0B);
        opcodesPerInstruction.put("JEZ", 0x0C);
        opcodesPerInstruction.put("JNE", 0x0D);
        opcodesPerInstruction.put("JLZ", 0x0E);
        opcodesPerInstruction.put("JLE", 0x0F);
        opcodesPerInstruction.put("JGZ", 0x10);
        opcodesPerInstruction.put("JGE", 0x11);
    }
}
