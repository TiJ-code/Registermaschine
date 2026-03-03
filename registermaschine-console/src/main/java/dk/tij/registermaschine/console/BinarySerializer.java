package dk.tij.registermaschine.console;

import dk.tij.registermaschine.core.compilation.api.compiling.*;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledInstruction;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledOperand;
import dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledProgram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BinarySerializer {
    private static final int MAGIC_NUMBER = 0x4A_41_53_4D;

    public static void save(Path path, ICompiledProgram program) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(path))) {
            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(program.size());

            for (ICompiledInstruction instr : program) {
                dos.writeByte(instr.opcode());

                ICompiledOperand[] operands = instr.operands();
                dos.writeByte(operands.length);

                for (ICompiledOperand op : operands) {
                    byte typeOrd = (byte) op.type().ordinal();
                    byte conceptOrd = (byte) op.concept().ordinal();
                    byte operandHeader = (byte) ((typeOrd << 4) | conceptOrd);
                    dos.writeByte(operandHeader);

                    dos.writeInt(op.value());
                }
            }
        }
    }

    public static ICompiledProgram load(Path path) throws IOException {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(path))) {
            if (dis.readInt() != MAGIC_NUMBER) {
                throw new IOException("Not a valid JASM binary file");
            }

            int instructionCount = dis.readInt();
            List<ICompiledInstruction> instructions = new ArrayList<>(instructionCount);

            for (int i = 0; i < instructionCount; i++) {
                byte opcode = dis.readByte();
                byte opCount = dis.readByte();

                ICompiledOperand[] operands = new ICompiledOperand[opCount];
                for (int j = 0; j < opCount; j++) {
                    byte operandHeader = dis.readByte();
                    int conceptOrd = operandHeader & 0x0F;
                    int typeOrd = (operandHeader & 0xF0) >> 4;

                    OperandType type = OperandType.values()[typeOrd];
                    OperandConcept concept = OperandConcept.values()[conceptOrd];
                    int value = dis.readInt();

                    operands[j] = new ConcreteCompiledOperand(type, concept, value);
                }

                instructions.add(new ConcreteCompiledInstruction(opcode, operands));
            }

            return new ConcreteCompiledProgram(instructions);
        }
    }
}
