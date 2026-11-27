package tij.registermaschine.api.registry;

import java.util.function.Supplier;

public interface IFactoryRegistry<T> {
    void register(String id, Supplier<? extends T> factory);

    T create(String id);
}
