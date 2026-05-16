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
 * Singleton class responsible for discovering, loading, registering
 * and enabling plugins from external JAR files.
 *
 * <p>Plugins are loaded through isolated {@link URLClassLoader}s and
 * must provide a valid plugin configuration file defined by
 * {@link PluginConstants#PLUGIN_FILENAME}.</p>
 *
 * <p>The loader follows a simple lifecycle:</p>
 * <ol>
 *     <li>{@link #init()}</li>
 *     <li>{@link #loadPlugins(Path)}</li>
 *     <li>{@link #enablePlugins()}</li>
 * </ol>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class PluginLoader {
    private static final ILogger LOGGER = LoggerFactory.getLogger(PluginLoader.class);

    /**
     * Singleton instance of the plugin loader.
     */
    private static final PluginLoader INSTANCE = new PluginLoader();

    /**
     * Stores all successfully loaded plugins and their configurations.
     */
    private final Map<PluginConfig, IPlugin> loadedPlugins = new ConcurrentHashMap<>();

    /**
     * Indicates whether the loader has been initialised.
     */
    private volatile boolean initialised = false;

    /**
     * Shared plugin context passed to all plugins during enabling.
     */
    private PluginContext pluginContext;

    /**
     * Private constructor to prevent external instantiation.
     */
    private PluginLoader() {}

    /**
     * Initialises the plugin loader and creates the shared plugin context.
     *
     * <p>This method must be called before any plugins can be loaded
     * or enabled.</p>
     */
    public void init() {
        LOGGER.debug("Initializing PluginLoader");
        this.pluginContext = new PluginContext(CoreConfig.STEP_HANDLER_REGISTRY);
        this.initialised = true;
    }

    /**
     * Loads all plugin JAR files from the given directory.
     *
     * <p>Only files ending in {@code .jar} are considered.
     * Invalid or incompatible plugins are skipped.</p>
     *
     * @param pluginsPath path to the plugin directory
     * @throws IllegalStateException if the loader has not been initialised
     */
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

    /**
     * Loads the single plugin from the specified JAR file.
     *
     * <p>The plugin JAR must contain a plugin configuration file
     * defined by {@link PluginConstants#PLUGIN_FILENAME}.
     * The plugin main class is loaded dynamically through a {@link URLClassLoader}.</p>
     *
     * @param pluginFile plugin JAR file
     * @throws IOException if the JAR file or configuration cannot be read
     * @throws ClassNotFoundException if the plugin main class cannot be found
     * @throws NoSuchMethodException if the plugin main class does not provide a no-argument constructor
     * @throws ClassInstantiationException if the plugin class cannot be instantiated
     * @throws InvocationTargetException if the constructor throws an exception
     * @throws InstantiationException if the plugin class is abstract
     * @throws IllegalAccessException if the constructor is inaccessible
     * @throws ParserConfigurationException if the XML parser configuration fails
     * @throws SAXException if the plugin configuration XML is invalid
     * @throws IllegalStateException if the loader has not been initialised
     */
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

    /**
     * Registers a loaded plugin and invokes its load callback.
     *
     * @param config plugin configuration
     * @param plugin plugin instance
     *
     * @throws IllegalStateException if the loader has not been initalised
     */
    public void registerPlugin(PluginConfig config, IPlugin plugin) {
        ensureInitialised();

        LOGGER.info("Registering plugin: \"{}:{}\"", config.name(), config.version());
        plugin.onLoad();
        loadedPlugins.put(config, plugin);
    }

    /**
     * Enables all previously loaded plugins.
     *
     * <p>Each plugin receives the shared {@link PluginContext}.</p>
     *
     * @throws IllegalStateException if the loader has not been initialised
     */
    public void enablePlugins() {
        ensureInitialised();

        LOGGER.info("Enabling all {} plugins!", loadedPlugins.size());
        loadedPlugins.values().forEach(p -> p.onEnable(pluginContext));
    }

    /**
     * Ensures that the plugin loader has been initialised.
     *
     * @throws IllegalStateException if the loader has not been initialised
     */
    private void ensureInitialised() {
        if (!initialised) {
            throw new IllegalStateException("PluginLoader has not been initialised");
        }
    }


    /**
     * Returns the singleton plugin loader instance.
     *
     * @return plugin loader instance
     */
    public static PluginLoader instance() {
        return INSTANCE;
    }
}
