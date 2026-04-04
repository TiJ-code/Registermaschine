package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.api.instructions.IStepHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class StepHandlerRegistry {
    private static final Map<Class<? extends IStepHandler>, IStepHandler> REGISTRY = new ConcurrentHashMap<>();

    public static void register(Class<? extends IStepHandler> clazz,  IStepHandler handler) {
        REGISTRY.put(clazz, handler);
    }

    public static IStepHandler get(Class<? extends IStepHandler> clazz) {
        return REGISTRY.get(clazz);
    }

    public static IStepHandler getOrCreate(Class<? extends IStepHandler> clazz,
                                           Function<Class<? extends IStepHandler>, IStepHandler> function) {
        return REGISTRY.computeIfAbsent(clazz, function);
    }
}
