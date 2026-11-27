package tij.registermaschine.api.registry;

import java.util.function.Supplier;

/**
 * Registers component factories by ID and creates components from them.
 *
 * @param <T> the component type
 * @since 2.0.0
 * @author TiJ
 */
public interface IFactoryRegistry<T> {
    /**
     * Registers a factory under the given ID.
     *
     * @param id the component ID
     * @param factory the factory used to create the component
     */
    void register(String id, Supplier<? extends T> factory);

    /**
     * Creates a component using the factory registered under the given ID.
     *
     * @param id the component ID
     * @return the created component
     */
    T create(String id);
}
