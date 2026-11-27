package tij.registermaschine.core;

import tij.registermaschine.api.registry.ComponentRegistries;
import tij.registermaschine.core.registry.FactoryRegistry;

/**
 * Provides the core assembly's shared component registries.
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class Components {
    /**
     * Default component ID.
     */
    public static final String DEFAULT = "core:default";

    /**
     * Private constructor to prevent initialisation.
     */
    private Components() {
    }

    /**
     * Shared registries passed to plugins during core startup.
     * Core startup registers a {@code core:default} factory in each registry.
     */
    public static final ComponentRegistries REGISTRIES = new ComponentRegistries(
            new FactoryRegistry<>(),
            new FactoryRegistry<>(),
            new FactoryRegistry<>(),
            new FactoryRegistry<>()
    );
}
