package dk.tij.registermaschine.api.plugin;

import dk.tij.registermaschine.api.instructions.IInstructionRegistry;

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
 * @param instructionRegistry the global instruction registry used for
 *                            registering and managing instructions
 *
 * @since 1.1.0
 * @author TiJ
 */
public record PluginContext(IInstructionRegistry instructionRegistry) {}
