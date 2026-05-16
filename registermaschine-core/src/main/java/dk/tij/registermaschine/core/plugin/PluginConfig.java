package dk.tij.registermaschine.core.plugin;

/**
 * Immutable data container representing the contents
 * of a plugin configuration file
 *
 * <p>A {@link PluginConfig} instance contains all metadata
 * requried to identify and load a plugin.</p>
 *
 * @param name display name of the plugin
 * @param description short description of the plugin
 * @param version plugin version string
 * @param author author or maintainer of the plugin
 * @param main fully qualified name of the plugin main class
 *
 * @since 1.1.0
 * @author TiJ
 */
public record PluginConfig(String name, String description, String version, String author, String main) {}
