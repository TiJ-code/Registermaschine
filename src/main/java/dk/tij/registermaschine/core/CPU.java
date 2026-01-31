package dk.tij.registermaschine.core;

import java.util.Scanner;

public class CPU implements ExecutionContext {
    private static final byte FLAG_ZERO = 0b0001,
                              FLAG_NEGATIVE = 0b0010,
                              FLAG_OVERFLOW = 0b0100;

    private final int[] registers = new int[8];
    private int programmeCounter = 0;

    private byte flags = 0;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public int getRegister(int index) {
        return registers[index];
    }

    @Override
    public void setRegister(int index, int value) {
        registers[index] = value;
    }

    @Override
    public int getProgrammeCounter() {
        return programmeCounter;
    }

    @Override
    public void setProgrammeCounter(int pc) {
        programmeCounter = pc;
    }

    @Override
    public boolean getNegativeFlag() {
        return (flags & FLAG_NEGATIVE) > 0;
    }

    @Override
    public boolean getZeroFlag() {
        return (flags & FLAG_ZERO) > 0;
    }

    @Override
    public boolean getOverflowFlag() {
        return (flags & FLAG_OVERFLOW) > 0;
    }

    @Override
    public void updateFlags(int[] operands, int operandCount, Integer result) {
        if (operands == null || operands.length == 0 || operandCount == 0) {
            setFlags(false, false, false);
            return;
        }

        if (result == null) {
            setFlags(false, false, false);
            return;
        }

        boolean negative = result < 0;
        boolean overFlow = false;
        if (operandCount >= 2) {
            overFlow = (operands[0] > 0 && operands[1] > 0 && negative) ||
                       (operands[0] < 0 && operands[1] < 0 && !negative);
        }

        setFlags(negative, result == 0, overFlow);
    }

    @Override
    public void setFlags(boolean negative, boolean zero, boolean overflow) {
        flags = 0;
        flags |= negative ? FLAG_NEGATIVE : 0;
        flags |= zero ? FLAG_ZERO     : 0;
        flags |= overflow ? FLAG_OVERFLOW : 0;
    }

    @Override
    public void output(int value) {
        System.out.println(value);
    }

    @Override
    public int input() {
        return Integer.parseInt(scanner.nextLine());
    }
}
