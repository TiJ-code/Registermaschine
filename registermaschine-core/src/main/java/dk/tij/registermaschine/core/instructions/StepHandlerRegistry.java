package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.api.instructions.IStepHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registry for {@link IStepHandler} instances.
 *
 * <p>This class manages a thread-safe mapping from step handler classes and
 * their singleton instances. It ensures that only one instance of a handler
 * exists per class, avoiding unnecessary object creation during instruction
 * execution.</p>
 *
 * <p>Typical usage includes registering custom step handlers, retrieving
 * existing handlers, or lazily creating a handler if it does not exist.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class StepHandlerRegistry {
    /**
     * Internal thread-safe registry mapping handler classes to their instances
     */
    private static final Map<Class<? extends IStepHandler>, IStepHandler> REGISTRY = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private StepHandlerRegistry() {}

    /**
     * Registers a handler instance for the specified class.
     *
     * @param clazz   the class of the handler
     * @param handler the handler instance to register
     */
    public static void register(Class<? extends IStepHandler> clazz,  IStepHandler handler) {
        REGISTRY.put(clazz, handler);
    }

    /**
     * Retrieves the registered handler instance for the specified class
     * @param clazz the class of the handler
     * @return the handler instance or {@code null} if it does not exist
     */
    public static IStepHandler get(Class<? extends IStepHandler> clazz) {
        return REGISTRY.get(clazz);
    }

    /**
     * Retrieves the handler instance for the specified class, creating and
     * registering it if it does not already exist.
     *
     * @param clazz    the class of the handler
     * @param function a factory function that creates a new handler instance
     *                 if one is not already registered
     * @return the existing or newly created {@link IStepHandler} instance
     */
    public static IStepHandler getOrCreate(Class<? extends IStepHandler> clazz,
                                           Function<Class<? extends IStepHandler>, IStepHandler> function) {
        return REGISTRY.computeIfAbsent(clazz, function);
    }
}
