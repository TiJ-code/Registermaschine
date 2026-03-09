package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.internal.instructions.CompiledInstructionPlan;
import dk.tij.registermaschine.core.compilation.internal.instructions.InstructionCompiler;
import dk.tij.registermaschine.core.error.UnknownInstructionException;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.*;

public final class ConcreteInstructionSet implements IInstructionSet {
    private final InstructionCompiler instructionCompiler = new InstructionCompiler();

    private final Map<String, ConfigInstruction> byName = new HashMap<>();
    private final Map<Byte, ConfigInstruction> byOpcode = new HashMap<>();
    private final Map<Byte, CompiledInstructionPlan> plans = new HashMap<>();

    @Override
    public void registerInstruction(ConfigInstruction configInstruction) {
        CompiledInstructionPlan plan = instructionCompiler.compile(configInstruction);
        plans.put(plan.opcode(), plan);
        byName.put(configInstruction.mnemonic(), configInstruction);
        byOpcode.put(configInstruction.opcode(), configInstruction);
    }

    @Override
    public void prohibitInstructionHandler(Class<? extends AbstractInstruction> instruction) {
        List<ConfigInstruction> prohibited = byOpcode.values().stream()
                .filter(Objects::nonNull)
                .filter(i -> i.steps().stream()
                        .anyMatch(s -> instruction.isAssignableFrom(s.handler().getClass())))
                .toList();

        for (ConfigInstruction instr : prohibited) {
            byName.remove(instr.mnemonic().toLowerCase(), instr);
            byOpcode.remove(instr.opcode(), instr);
            plans.remove(instr.opcode());
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
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
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
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
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
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
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
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.steps().getFirst().handler();
    }

    @Override
    public String getMnemonic(byte opcode) {
        ConfigInstruction entry = byOpcode.get(opcode);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with opcode " + opcode);
        return entry.mnemonic();
    }

    @Override
    public byte getOpcode(String mnemonic) {
        ConfigInstruction entry = byName.get(mnemonic);
        if (entry == null)
            throw new UnknownInstructionException("No instruction found with mnemonic " + mnemonic);
        return entry.opcode();
    }

    @Override
    public boolean contains(String mnemonic) {
        return byName.containsKey(mnemonic);
    }

    @Override
    public boolean contains(byte opcode) {
        return byOpcode.containsKey(opcode);
    }

    @SuppressWarnings("markedForRemoval")
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public List<ConfigInstruction> getInstructions() {
        return List.copyOf(byOpcode.values());
    }

    @Override
    public CompiledInstructionPlan getPlan(byte opcode) {
        return plans.get(opcode);
    }

    public List<CompiledInstructionPlan> getInstructionPlans() {
        return List.copyOf(plans.values());
    }
}
