package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.instructions.api.ChainedInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.instructions.api.IStepHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConcreteInstructionSet implements IInstructionSet {
    private static final int BYTE_BITMASK = 0xFF;

    private final Map<Integer, Pair<ConfigInstruction, ChainedInstruction>> opcodeMapping = new HashMap<>();
    private final Map<Class<? extends IStepHandler>, List<Integer>> handlerIndex = new HashMap<>();

    @Override
    public void register(ConfigInstruction configInstruction, ChainedInstruction chainedInstruction) {
        final int finalOpcode = configInstruction.opcode() & BYTE_BITMASK;

        var pair = opcodeMapping.computeIfAbsent(finalOpcode, _ -> new Pair<>(configInstruction));
        pair.value = chainedInstruction;

        Arrays.stream(chainedInstruction.steps()).forEach(step -> {
            Class<? extends IStepHandler> handlerClass = step.handler().getClass();

            handlerIndex
                    .computeIfAbsent(handlerClass, _ -> new ArrayList<>())
                    .add(finalOpcode);
        });
    }

    @Override
    public ConfigInstruction getConfigInstruction(int opcode) {
        return opcodeMapping.get(opcode & BYTE_BITMASK).key();
    }

    @Override
    public ConfigInstruction getConfigInstruction(String mnemonic) {
        return opcodeMapping.values().stream()
                .filter(p -> p.key().mnemonic().equals(mnemonic))
                .findFirst()
                .orElse(Pair.empty())
                .key();
    }

    @Override
    public ChainedInstruction get(int opcode) {
        return opcodeMapping.get(opcode & BYTE_BITMASK).value;
    }

    @Override
    public void prohibitInstructionHandler(Class<? extends IStepHandler> handlerClass) {
        for (var entry : handlerIndex.entrySet()) {
            if (!handlerClass.isAssignableFrom(entry.getKey()))
                continue;

            for (int opcode : entry.getValue()) {
                opcodeMapping.remove(opcode);
            }
        }
    }

    @Override
    public boolean contains(String mnemonic) {
        return getConfigInstructions().stream().anyMatch(c -> c.mnemonic().equalsIgnoreCase(mnemonic));
    }

    @Override
    public boolean contains(int opcode) {
        return opcodeMapping.containsKey(opcode & BYTE_BITMASK);
    }

    @Override
    public List<ConfigInstruction> getConfigInstructions() {
        return opcodeMapping.values().stream().map(Pair::key).collect(Collectors.toList());
    }

    @Override
    public List<ChainedInstruction> getInstructions() {
        return opcodeMapping.values().stream().map(Pair::value).collect(Collectors.toList());
    }

    static class Pair<K, V> {
        static <K, V> Pair<K, V> empty() {
            return new Pair<>(null);
        }

        private final K key;
        private V value;

        Pair(K key) {
            this.key = key;
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }
    }
}
