package tij.registermaschine.core;

import tij.registermaschine.api.registry.ComponentRegistries;
import tij.registermaschine.core.registry.FactoryRegistry;

public final class Components {
    private Components() {}

    public static final ComponentRegistries REGISTRIES = new ComponentRegistries(
            new FactoryRegistry<>(),
            new FactoryRegistry<>(),
            new FactoryRegistry<>(),
            new FactoryRegistry<>()
    );
}
