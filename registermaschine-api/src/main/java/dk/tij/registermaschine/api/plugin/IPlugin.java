package dk.tij.registermaschine.api.plugin;

/**
 * @since 1.1.0
 * @author TiJ
 */
public interface IPlugin {
    void onLoad();
    void onEnable(PluginContext context);
    void onDisable();
}
