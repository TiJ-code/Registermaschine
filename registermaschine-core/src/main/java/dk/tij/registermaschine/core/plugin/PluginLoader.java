package dk.tij.registermaschine.core.plugin;

import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.api.plugin.IPlugin;
import dk.tij.registermaschine.api.plugin.PluginContext;
import dk.tij.registermaschine.core.config.CoreConfig;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @since 1.1.0
 * @author TiJ
 */
public final class PluginLoader {
    private static final ILogger LOGGER = LoggerFactory.getLogger(PluginLoader.class);

    private static final PluginLoader INSTANCE = new PluginLoader();

    private final Map<PluginConfig, IPlugin> loadedPlugins = new ConcurrentHashMap<>();
    private volatile boolean initialised = false;

    private PluginContext pluginContext;

    private PluginLoader() {}

    public void init() {
        LOGGER.debug("Initializing PluginLoader");
        this.pluginContext = new PluginContext(CoreConfig.INSTRUCTION_REGISTRY);
        this.initialised = true;
    }

    public void loadPlugins(Path pluginsPath) {
        ensureInitialised();

        LOGGER.debug("Loading possible plugin files");
        File[] jars = new File(pluginsPath.toUri()).listFiles(f -> f.getName().endsWith(".jar"));

        if (jars == null)
            return;

        LOGGER.info("Found {} possible plugin files", jars.length);

        try {
            for (File jar : jars) {
                LOGGER.trace("Trying to load plugin \"{}\"", jar.getName());
                loadPlugin(jar);
                LOGGER.trace("Done loading plugin \"{}\"", jar.getName());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void loadPlugin(File pluginFile)
            throws IOException, ClassNotFoundException, NoSuchMethodException, ClassInstantiationException,
                   InvocationTargetException, InstantiationException, IllegalAccessException,
                   ParserConfigurationException, SAXException {
        ensureInitialised();
        if (!initialised) {
            throw new IllegalStateException("PluginLoader has not been initialised");
        }

        try (JarFile jf = new JarFile(pluginFile)) {
            JarEntry entry = jf.getJarEntry(PluginConstants.PLUGIN_FILENAME);
            if (entry == null)
                return;

            try (InputStream is = jf.getInputStream(entry)) {
                PluginConfig config = PluginConfigParser.instance().parse(is);

                URLClassLoader cl = new URLClassLoader(
                        new URL[]{pluginFile.toURI().toURL()},
                        getClass().getClassLoader()
                );

                Class<?> clazz = Class.forName(config.main(), true, cl);
                IPlugin plugin = (IPlugin) clazz.getDeclaredConstructor().newInstance();

                registerPlugin(config, plugin);
            }
        }
    }

    public void registerPlugin(PluginConfig config, IPlugin plugin) {
        ensureInitialised();

        LOGGER.info("Registering plugin: \"{}:{}\"", config.name(), config.version());
        plugin.onLoad();
        loadedPlugins.put(config, plugin);
    }

    public void enablePlugins() {
        ensureInitialised();

        LOGGER.info("Enabling all {} plugins!", loadedPlugins.size());
        loadedPlugins.values().forEach(p -> p.onEnable(pluginContext));
    }

    private void ensureInitialised() {
        if (!initialised) {
            throw new IllegalStateException("PluginLoader has not been initialised");
        }
    }

    public static PluginLoader instance() {
        return INSTANCE;
    }
}
