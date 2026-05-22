package dk.tij.registermaschine.api.plugin;

/**
 * Represents a Registermaschine plugin lifecycle contract.
 *
 * <p>Implementations of this interface are discovered and managed by the
 * Registermaschine plugin system. The lifecycle methods are invoked in
 * the following order</p>
 * <ol>
 *     <li>{@link #onLoad()}</li>
 *     <li>{@link #onEnable(PluginContext)} ()}</li>
 *     <li>{@link #onDisable()} ()} ()}</li>
 * </ol>
 *
 * <p>Plugins should use:</p>
 * <ul>
 *     <li>{@link #onLoad()} for lightweight initialisation and resource loading</li>
 *     <li>{@link #onEnable(PluginContext)} for registering commands, extensions,
 *     listeners, or services</li>
 *     <li>{@link #onDisable()} for cleanup and shutdown logic</li>
 * </ul>
 *
 * <p>Implementation should avoid blocking operations during lifecycle execution unless
 * explicitly required.</p>
 *
 * @since 1.1.0
 * @author TiJ
 */
public interface IPlugin {
    /**
     * Called when the plugin is loaded by the plugin manager.
     *
     * <p>This method is invoked before the plugin becomes active.
     * It should be used for lightweight setup operations such as:</p>
     * <ul>
     *     <li>loading configuration files</li>
     *     <li>preparing internal state</li>
     *     <li>validating environment requirements</li>
     * </ul>
     *
     * <p>At this stage, the plugin should not interact with runtime
     * systems that require an enabled plugin state.</p>
     */
    void onLoad();

    /**
     * Called when the plugin is enabled and ready for interaction.
     *
     * <p>This method is invoked after {@link #onLoad()} completed successfully.
     * The provided {@link PluginContext} grants access to runtime environment
     * and plugin-related services.</p>
     *
     * <p>Typical use cases include:</p>
     * <ul>
     *     <li>registering commands</li>
     *     <li>registering instruction extensions</li>
     *     <li>starting background services</li>
     *     <li>hooking into events or APIs</li>
     * </ul>
     *
     * @param context the plugin runtime context
     */
    void onEnable(PluginContext context);

    /**
     * Called before the plugin is unloaded or the application shuts down.
     *
     * <p>Implementations should release allocated resources and stop any
     * running services or threads.</p>
     *
     * <p>Typical cleanup operations include:</p>
     * <ul>
     *     <li>saving persistent state</li>
     *     <li>closing files or network connections</li>
     *     <li>stopping scheduled tasks</li>
     *     <li>unregistering listeners</li>
     * </ul>
     */
    void onDisable();
}
