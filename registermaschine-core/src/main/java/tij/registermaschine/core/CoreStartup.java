package tij.registermaschine.core;

import tij.registermaschine.api.config.IConfigParser;
import tij.registermaschine.api.log.ILogger;
import tij.registermaschine.api.log.LoggerFactory;
import tij.registermaschine.api.plugin.PluginContext;
import tij.registermaschine.core.config.CoreConfig;
import tij.registermaschine.core.config.CoreConfigParser;
import tij.registermaschine.pluginloader.PluginConfigParser;
import tij.registermaschine.pluginloader.PluginLoader;

/**
 * Initialises configuration and starts the plugin system.
 * Call this instead of {@link CoreConfigParser#init(IConfigParser...)} when plugins are needed.
 */
public final class CoreStartup {
    private static final ILogger LOGGER = LoggerFactory.getLogger(CoreStartup.class);
    private static boolean started;

    private CoreStartup() {}

    public static void init(IConfigParser... postConfigParsers) {
        init(false, postConfigParsers);
    }

    public static synchronized void init(boolean onlyInternal, IConfigParser... postConfigParsers) {
        if (started) return;

        CoreConfigParser.init(onlyInternal, postConfigParsers);
        PluginLoader.instance().init(new PluginContext(CoreConfig.STEP_HANDLER_REGISTRY));
        try {
            LOGGER.info("loading plugins");
            PluginLoader.instance().loadPlugins(PluginConfigParser.PLUGIN_PATH);
        } catch (Exception e) {
            LOGGER.error("Error loading plugins", e);
            PluginLoader.instance().disablePlugins();
        }
        PluginLoader.instance().enablePlugins();
        started = true;
    }
}