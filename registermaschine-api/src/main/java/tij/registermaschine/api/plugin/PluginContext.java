package tij.registermaschine.api.plugin;

import tij.registermaschine.api.instructions.IStepHandlerRegistry;
import tij.registermaschine.api.registry.ComponentRegistries;

/**
 * Represents the runtime context provided to plugins during enablement.
 *
 * <p>The plugin context exposes access to core systems and registries
 * required for interacting with the Registermaschine runtime.</p>
 *
 * <p>An instance of this record is passed to
 * {@link IPlugin#onEnable(PluginContext)} when a plugin is enabled</p>
 *
 * <p>Additional runtime services and registries may be added in future
 * API versions</p>
 *
 * @param stepHandlerRegistry the global registry used for registering
 *                            and managing step handlers
 *
 * @since 1.1.0
 * @author TiJ
 */
public record PluginContext(
        IStepHandlerRegistry stepHandlerRegistry,
        ComponentRegistries componentRegistries
) {}
