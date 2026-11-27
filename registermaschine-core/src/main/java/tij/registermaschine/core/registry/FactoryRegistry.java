package tij.registermaschine.core.registry;

import tij.registermaschine.api.registry.IFactoryRegistry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Thread-safe factory registry backed by a concurrent map.
 *
 * <p>Each ID can be registered only once. Each call to {@link #create(String)}
 * invokes the registered factory. The factory decides whether to return a new
 * instance or an existing one.</p>
 *
 * @param <T> the component type
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class FactoryRegistry<T> implements IFactoryRegistry<T> {
    private final Map<String, Supplier<? extends T>> factories = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@code id} or {@code factory} is {@code null}
     * @throws IllegalArgumentException if {@code id} is blank or already registered
     */
    @Override
    public void register(String id, Supplier<? extends T> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Component ID cannot be blank");
        }

        if (factories.putIfAbsent(id, factory) != null) {
            throw new IllegalArgumentException("Component ID '" + id + "' already registered");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@code id} is null
     * @throws IllegalArgumentException if the ID is unknown or its factory returns null
     */
    @Override
    public T create(String id) {
        Objects.requireNonNull(id, "id");

       Supplier<? extends T> factory = factories.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown component ID: '" + id + "'");
        }

        T instance = factory.get();
        if (instance == null) {
            throw new IllegalArgumentException("Factory returned null for component ID: '" + id + "'");
        }

        return instance;
    }
}
