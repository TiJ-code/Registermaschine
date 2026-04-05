package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.instructions.IInstructionRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.1.0
 * @author TiJ
 */
public final class ConcreteInstructionRegistry implements IInstructionRegistry {
    private static final ConcreteInstructionRegistry INSTANCE = new ConcreteInstructionRegistry();

    private final Map<String, Class<? extends AbstractInstruction>> registry = new ConcurrentHashMap<>();
    private final Set<String> prohibited = ConcurrentHashMap.newKeySet();

    private ConcreteInstructionRegistry() {}

    @Override
    public void register(Class<? extends AbstractInstruction> handlerClass) {
        if (handlerClass == null) {
            throw new IllegalArgumentException("Instruction handler cannot be null");
        }

        if (contains(handlerClass.getName())) {
            throw new IllegalStateException("Instruction handler %s already registered".formatted(handlerClass));
        }

        registry.put(handlerClass.getName(), handlerClass);
    }

    @Override
    public boolean contains(String handler) {
        return registry.containsKey(handler);
    }

    @Override
    public AbstractInstruction instantiate(String className,
                                           byte opcode, int operandCount, ICondition condition)
            throws ClassInstantiationException {
        Class<? extends AbstractInstruction> clazz = registry.get(className);

        if (clazz == null) {
            throw new IllegalStateException("Instruction handler %s is not registered".formatted(className));
        }

        if (prohibited.contains(className))
            throw new IllegalStateException("Instruction handler %s is prohibited".formatted(className));

        try {
            return clazz
                    .getDeclaredConstructor(byte.class, int.class, ICondition.class)
                    .newInstance(opcode, operandCount, condition);
        } catch (Exception e) {
            throw new ClassInstantiationException("Could not instantiate %s".formatted(className), e);
        }
    }

    @Override
    public void prohibit(String className) {
        if (!contains(className)) {
            throw new IllegalArgumentException("Instruction handler %s is  not registered".formatted(className));
        }
        prohibited.add(className);
    }

    public static ConcreteInstructionRegistry instance() {
        return INSTANCE;
    }
}
