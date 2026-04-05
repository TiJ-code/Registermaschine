package dk.tij.registermaschine.core.plugin;

import dk.tij.registermaschine.api.error.ClassInstantiationException;
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
    private static final PluginLoader INSTANCE = new PluginLoader();

    private final Map<PluginConfig, IPlugin> loadedPlugins = new ConcurrentHashMap<>();
    private volatile boolean initialised = false;

    private PluginContext pluginContext;

    private PluginLoader() {}

    public void init() {
        this.pluginContext = new PluginContext(CoreConfig.INSTRUCTION_REGISTRY);
        this.initialised = true;
    }

    public void loadPlugins(Path pluginsPath) {
        ensureInitialised();

        File[] jars = new File(pluginsPath.toUri()).listFiles(f -> f.getName().endsWith(".jar"));

        if (jars == null)
            return;

        try {
            for (File jar : jars) {
                loadPlugin(jar);
            }
        } catch (Exception e) {
            System.err.printf("[dk.tij.registermaschine.core.plugin.PluginLoader] %s%n", e.getMessage());
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

        System.out.printf("loading plugin: \"%s:%s\"%n", config.name(), config.version());
        plugin.onLoad();
        loadedPlugins.put(config, plugin);
    }

    public void enablePlugins() {
        ensureInitialised();

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
