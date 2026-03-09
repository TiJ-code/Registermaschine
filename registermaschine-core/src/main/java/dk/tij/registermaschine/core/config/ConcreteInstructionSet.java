package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.internal.instructions.CompiledInstructionPlan;
import dk.tij.registermaschine.core.compilation.internal.instructions.InstructionCompiler;
import dk.tij.registermaschine.core.error.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.*;
import java.util.stream.Stream;

public final class ConcreteInstructionSet implements IInstructionSet {
    private static final int BYTE_BITMASK = 0xFF;

    private final InstructionCompiler instructionCompiler = new InstructionCompiler();

    private final CompiledInstructionPlan[] plans = new CompiledInstructionPlan[BYTE_BITMASK];
    private final ConfigInstruction[] byOpcode = new ConfigInstruction[BYTE_BITMASK];

    @Override
    public void registerInstruction(ConfigInstruction configInstruction) {
        CompiledInstructionPlan plan = instructionCompiler.compile(configInstruction);

        final int idx = plan.opcode() & BYTE_BITMASK;

        plans[idx] = plan;
        byOpcode[idx] = configInstruction;
    }

    @Override
    public void prohibitInstructionHandler(Class<? extends AbstractInstruction> instruction) {
        List<ConfigInstruction> prohibited = Stream.of(byOpcode)
                .filter(Objects::nonNull)
                .filter(i -> i.steps().stream()
                        .anyMatch(s -> instruction.isAssignableFrom(s.handler().getClass())))
                .toList();

        for (ConfigInstruction instr : prohibited) {
            final int idx = instr.opcode() & BYTE_BITMASK;
            byOpcode[idx] = null;
            plans[idx] = null;
        }
    }

    /**
     * Returns configured instruction instance based on its mnemonic.
     *
     * @param mnemonic the mnemonic of the instruction
     * @return the configured instruction
     *
     * @since 2.0.0
     */
    @Override
    public ConfigInstruction getInstruction(String mnemonic) {
        ConfigInstruction entry = Stream.of(byOpcode)
                .filter(i -> i.mnemonic().equals(mnemonic))
                .findAny()
                .orElse(null);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic %s"
                    .formatted(mnemonic));
        return entry;
    }

    /**
     * Returns configured instruction instance based on its opcode.
     *
     * @param opcode the opcode of the instruction
     * @return the configured instruction
     *
     * @since 2.0.0
     */
    @Override
    public ConfigInstruction getInstruction(byte opcode) {
        final int idx = opcode & BYTE_BITMASK;

        ConfigInstruction entry = byOpcode[idx];
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode %d."
                    .formatted(idx));
        return entry;
    }

    /**
     * +++ DEPRECATED +++
     * <p>use {@link ConcreteInstructionSet#getInstruction(String mnemonic)} instead</p>
     * +++ DEPRECATED +++
     * <p>Returns the very first handler by its instruction mnemonic</p>
     *
     * @param mnemonic the mnemonic of the instruction
     * @return the handler of the very first step
     *
     * @deprecated This is deprecated since the introduction of multiple instruction handlers in {@code 2.0.0}
     * @since 1.0.0
     */
    @Deprecated
    @Override
    public AbstractInstruction getHandler(String mnemonic) {
        ConfigInstruction entry = lookUpByMnemonic(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic %s."
                    .formatted(mnemonic));
        return entry.steps().getFirst().handler();
    }

    /**
     * +++ DEPRECATED +++
     * <p>use {@link ConcreteInstructionSet#getInstruction(byte opcode)} instead</p>
     * +++ DEPRECATED +++
     * <p>Returns the very first handler by its instruction opcode</p>
     *
     * @param opcode the opcode of the instruction
     * @return the handler of the very first step
     *
     * @deprecated This is deprecated since the introduction of multiple instruction handlers in {@code 2.0.0}
     * @since 1.0.0
     */
    @Deprecated
    @Override
    public AbstractInstruction getHandler(byte opcode) {
        final int idx = opcode & BYTE_BITMASK;

        ConfigInstruction entry = byOpcode[idx];
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode %d."
                    .formatted(idx));
        return entry.steps().getFirst().handler();
    }

    @Override
    public String getMnemonic(byte opcode) {
        final int idx = opcode & BYTE_BITMASK;

        ConfigInstruction entry = byOpcode[idx];
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode %d."
                    .formatted(idx));
        return entry.mnemonic();
    }

    @Override
    public byte getOpcode(String mnemonic) {
        ConfigInstruction entry = lookUpByMnemonic(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic %s."
                    .formatted(mnemonic));
        return entry.opcode();
    }

    @Override
    public boolean contains(String mnemonic) {
        return lookUpByMnemonic(mnemonic) != null;
    }

    @Override
    public boolean contains(byte opcode) {
        return byOpcode[opcode & BYTE_BITMASK] != null;
    }

    @SuppressWarnings("markedForRemoval")
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public List<ConfigInstruction> getInstructions() {
        return Arrays.stream(byOpcode).filter(Objects::nonNull).toList();
    }

    @Override
    public CompiledInstructionPlan getPlan(byte opcode) {
        return plans[opcode & BYTE_BITMASK];
    }

    public List<CompiledInstructionPlan> getInstructionPlans() {
        return Arrays.stream(plans).filter(Objects::nonNull).toList();
    }

    private ConfigInstruction lookUpByMnemonic(String mnemonic) {
        for (ConfigInstruction instr : byOpcode) {
            if (instr != null && instr.mnemonic().equals(mnemonic))
                return instr;
        }
        return null;
    }
}
