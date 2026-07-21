package dk.tij.rm;

import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.api.plugin.IPlugin;
import dk.tij.registermaschine.api.plugin.PluginContext;

/**
 * @since 1.1.0
 * @author TiJ
 */
public class RegistermaschinePlugin implements IPlugin {
    private static final ILogger LOGGER = LoggerFactory.getLogger(RegistermaschinePlugin.class);

    public RegistermaschinePlugin() {}

    @Override
    public void onLoad() {
        LOGGER.info("Loading");
    }

    @Override
    public void onEnable(PluginContext context) {
        LOGGER.info("Enabling instructions...");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Disabling");
    }
}
