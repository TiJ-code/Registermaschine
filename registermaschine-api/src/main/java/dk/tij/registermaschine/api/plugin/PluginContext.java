package dk.tij.registermaschine.api.plugin;

import dk.tij.registermaschine.api.instructions.IInstructionRegistry;

/**
 * @since 1.1.0
 * @author TiJ
 */
public record PluginContext(IInstructionRegistry instructionRegistry) {}
