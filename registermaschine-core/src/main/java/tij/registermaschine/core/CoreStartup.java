package tij.registermaschine.core;

import tij.registermaschine.api.compilation.lexing.TokenType;
import tij.registermaschine.api.config.IConfigParser;
import tij.registermaschine.api.log.ILogger;
import tij.registermaschine.api.log.LoggerFactory;
import tij.registermaschine.api.plugin.PluginContext;
import tij.registermaschine.api.registry.ComponentRegistries;
import tij.registermaschine.compiler.CompilerOptions;
import tij.registermaschine.compiler.ConcreteCompiler;
import tij.registermaschine.compiler.ConcreteLexer;
import tij.registermaschine.compiler.ConcreteParser;
import tij.registermaschine.core.config.CoreConfig;
import tij.registermaschine.core.config.CoreConfigParser;
import tij.registermaschine.engine.instructions.ConcreteInstructionSet;
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

    /**
     * Initialises configuration, registers default component factories and starts plugins.
     * Subsequent calls return without repeating the startup after it has completed.
     *
     * <p>The default lexer and compiler factories use options captured during startup.
     * Custom factories must be selected by ID when using the compilation pipeline.</p>
     *
     * @param postConfigParsers parsers to execute after internal configuration parsing
     */
    public static synchronized void init(IConfigParser... postConfigParsers) {
        if (started) return;

        CoreConfigParser.init(postConfigParsers);

        ComponentRegistries components = Components.REGISTRIES;

        CompilerOptions options = new CompilerOptions(CoreConfig.REGISTERS, CoreConfig.ALLOW_LABELS, CoreConfig.TOKEN_REGEX.get(TokenType.REGISTER));

        components.lexers().register("core:default", () -> new ConcreteLexer(options));
        components.parsers().register("core:default", ConcreteParser::new);
        components.compilers().register("core:default", () -> new ConcreteCompiler(options));
        components.instructionSets().register("core:default", ConcreteInstructionSet::new);
        components.tokens().register(Components.DEFAULT, ConcreteLexer::defaultTokenFactory);
        components.compiledInstructions().register(Components.DEFAULT, ConcreteCompiler::defaultInstructionFactory);

        PluginLoader.instance().init(new PluginContext(CoreConfig.STEP_HANDLER_REGISTRY, components));
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