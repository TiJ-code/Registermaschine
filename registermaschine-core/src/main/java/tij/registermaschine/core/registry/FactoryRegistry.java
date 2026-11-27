package tij.registermaschine.core.registry;

import tij.registermaschine.api.registry.IFactoryRegistry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class FactoryRegistry<T> implements IFactoryRegistry<T> {
    private final Map<String, Supplier<? extends T>> factories = new ConcurrentHashMap<>();

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
