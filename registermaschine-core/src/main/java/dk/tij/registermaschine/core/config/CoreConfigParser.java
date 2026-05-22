package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.config.IConfigEventListener;
import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.instructions.IInstructionSet;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LogLevel;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.internal.parsers.ConditionMacroParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionParser;
import dk.tij.registermaschine.core.config.internal.parsers.SettingsParser;
import dk.tij.registermaschine.core.plugin.PluginConfigParser;
import dk.tij.registermaschine.core.plugin.PluginLoader;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Core configuration parser for the Registermaschine.
 *
 * <p>This class handles the initialisation and parsing of the core configuration XML files,
 * instruction sets, and default condition macros. It ensures thread-safe single initialisation
 * of the core configuration.</p>
 *
 * <p>It provides:</p>
 * <ul>
 *     <li>Loading of the main configuration file</li>
 *     <li>Parsing of default and custom instruction sets</li>
 *     <li>Loading and parsing of core condition macros</li>
 *     <li>Support for post-configuration parsers</li>
 *     <li>Optional custom root path for user-editable files</li>
 * </ul>
 *
 * <p>All XML parsing is validated against internal DTDs. Missing files are either copied
 * from resources or a {@link ConfigurationParseException} is thrown for critical failures.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class CoreConfigParser {
    private static ILogger LOGGER = LoggerFactory.getLogger(CoreConfigParser.class);

    /**
     * Name of the instruction parser target
     */
    public static final String  PARSER_INSTRUCTIONS = InstructionParser.class.getName(),
                                PARSER_CONFIGURATION = SettingsParser.class.getName();

    /**
     * Internal DTD names
     */
    public static final String  DTD_CONFIGURATION = "dtd/configuration.dtd",
                                DTD_INSTRUCTION_SET = "dtd/instruction_file.dtd";

    /**
     * Default core configuration files
     */
    private static final String CONFIGURATION_FILE = "configuration.jxml",
                                DEFAULT_CONDITION_MACROS_FILE = "core_condition_macros.jxml",
                                DEFAULT_INSTRUCTION_SET_FILE = "default.instructions.jxml";

    /**
     * Optional custom root path for user-editable configuration files
     */
    private static Path ROOT_PATH = Paths.get(".");

    /**
     * Flag indicating whether the core configuration has been successfully parsed
     */
    private static volatile boolean coreConfigParsed = false;
    /**
     * Lock for thread-safe initialisation
     */
    private static final Object INIT_LOCK = new Object();

    /**
     * Internal parsers executed during core configuration initialisation
     */
    private static final List<IConfigParser> INTERNAL_CONFIG_PARSERS = List.of(
            new SettingsParser()
    );
    /**
     * Parser for condition macros
     */
    private static final IConfigParser MACRO_PARSER = new ConditionMacroParser();
    /**
     * Parser for instruction sets
     */
    private static final IConfigParser INSTRUCTION_PARSER = new InstructionParser();

    /**
     * Private constructor to prevent instantiation
     */
    private CoreConfigParser() {}

    /**
     * Initialises the core configuration.
     *
     * <p>This method parses the main configuration file, applies internal parses and
     * optionally applies post-configuration parsers provided by the caller.</p>
     *
     * @param postConfigParsers optional parsers to execute after internal configuration parsing
     * @throws ConfigurationParseException if any parsing or I/O errors occur
     */
    public static void init(IConfigParser... postConfigParsers) throws ConfigurationParseException {
        init(false, postConfigParsers);
    }

    /**
     * Initialises the core configuration with an option to skip external files
     *
     * @param onlyInternal if {@code true}, only internal resources are loaded; no user-editable files are copied
     * @param postConfigParsers optional parses to execute after internal configuration parsing
     * @throws ConfigurationParseException if any parsing or I/O errors occurs
     */
    public static void init(boolean onlyInternal, IConfigParser... postConfigParsers) {
        if (coreConfigParsed) {
            LOGGER.trace("Core configuration already parsed. Skipping init");
            return;
        }

        synchronized (INIT_LOCK) {
            if (coreConfigParsed) {
                LOGGER.trace("Core configuration already parsed. Skipping init");
                return;
            }

            CoreConfig.LOG.setGlobalMinimumLevel(LogLevel.ERROR);
            LOGGER.setPath(ROOT_PATH);
            LOGGER.flog("Setting up logger");

            if (!onlyInternal) {
                LOGGER.info("Copying default configuration files to root path {}", ROOT_PATH);
                copyDefaultFiles();
            }

            try (InputStream is = getSmartStream(CONFIGURATION_FILE, onlyInternal)) {
                LOGGER.debug("Parsing core configuration file {}", CONFIGURATION_FILE);
                Document doc = parseWithDtd(is, DTD_CONFIGURATION);

                LOGGER.debug("Running internal config parsers");
                INTERNAL_CONFIG_PARSERS.forEach(parser -> {
                    LOGGER.trace("Parsing with internal parser {}", parser.getClass().getName());
                    parser.parseConfig(doc);
                });
                LOGGER.debug("Finished internal config parsers");

                if (postConfigParsers != null) {
                    LOGGER.debug("Running post config parsers");
                    Arrays.stream(postConfigParsers)
                            .filter(Objects::nonNull)
                            .forEach(parser -> {
                                LOGGER.trace("Parsing with post parser {}", parser.getClass().getName());
                                parser.parseConfig(doc);
                            });
                    LOGGER.debug("Finished post config parsers");
                }

                coreConfigParsed = true;
                LOGGER.info("Core configuration successfully initialised");
            } catch (Exception e) {
                LOGGER.error("Failed to initialise core configuration", e);
                throw new ConfigurationParseException("Failed to initialise core configuration", e);
            }

            loadDefaultMacros();

            PluginLoader.instance().init();
            try {
                LOGGER.info("loading plugins");
                PluginLoader.instance().loadPlugins(PluginConfigParser.PLUGIN_PATH);
            } catch (Exception e) {
                LOGGER.error("Error loading plugins", e);
                PluginLoader.instance().disablePlugins();
            }

            PluginLoader.instance().enablePlugins();
        }
    }

    /**
     * Parses the default instruction set into the provided {@link IInstructionSet}.
     *
     * @param set the instruction set to populate
     * @throws ConfigurationParseException if parsing fails
     */
    public static void parseDefaultInstructionSet(IInstructionSet set) throws ConfigurationParseException {
        parseInstructionSet(DEFAULT_INSTRUCTION_SET_FILE, set);
    }

    /**
     * Parses a specific instruction set XML file into the provided {@link IInstructionSet}
     *
     * @param fileName the instruction set XML filename
     * @param set the instruction set to populate
     * @throws ConfigurationParseException if parsing fails
     * @throws IllegalStateException if the core configuration has not been initialised
     */
    public static void parseInstructionSet(String fileName, IInstructionSet set)
            throws ConfigurationParseException {
        if (!coreConfigParsed) {
            final String errorMsg = "Core Configuration must be initialised via init() before parsing instruction sets.";
            LOGGER.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        LOGGER.info("Parsing instruction set file {}", fileName);

        try (InputStream is = getSmartStream(fileName)) {
            Document doc = parseWithDtd(is, DTD_INSTRUCTION_SET);

            CoreConfig.INSTRUCTIONS.clear();
            LOGGER.trace("Cleared existing instructions");

            LOGGER.debug("Parsing instructions");
            INSTRUCTION_PARSER.parseConfig(doc);
            LOGGER.debug("Finished instruction parsing");

            LOGGER.debug("Parsing macros for instruction set");
            MACRO_PARSER.parseConfig(doc);
            LOGGER.debug("Finished macro parsing");

            CoreConfig.INSTRUCTIONS.forEach(instr -> {
                set.registerInstruction(instr);
                LOGGER.trace("Registered instruction '{}' with opcode {}", instr.mnemonic(), instr.opcode());
            });

            LOGGER.info("All instructions registered to {}", set);
        } catch (Exception e) {
            final String errorMsg = "Failed to parse instruction set: " + fileName;
            LOGGER.error(errorMsg, e);
            throw new ConfigurationParseException(errorMsg, e);
        }
    }

    /**
     * Adds a configuration event listener to a specific parser target.
     *
     * @param target the parser target name
     * @param listener the listener to register
     */
    public static void addListenerToTarget(String target, IConfigEventListener listener) {
        LOGGER.trace("Registering listener {} to target {}", listener, target);
        if (PARSER_INSTRUCTIONS.equals(target))
            INSTRUCTION_PARSER.addListener(listener);
        else if (PARSER_CONFIGURATION.equals(target))
            INTERNAL_CONFIG_PARSERS.stream()
                    .filter(p -> p instanceof SettingsParser)
                    .findFirst()
                    .orElseThrow()
                    .addListener(listener);
    }

    /**
     * Sets a custom root path for user-editable configuration files.
     *
     * @param customPath the root path
     */
    public static void setCustomRootPath(Path customPath) {
        LOGGER.trace("Setting custom root path to {}", customPath);
        ROOT_PATH = customPath;
    }

    /**
     * Copies the default configuration files to the user-editable path if they do not exist.
     */
    private static void copyDefaultFiles() {
        List<String> userEditableFiles = List.of(
                CONFIGURATION_FILE, DEFAULT_INSTRUCTION_SET_FILE
        );

        for (String fileName : userEditableFiles) {
            File targetFile;
            if (ROOT_PATH != null)
                targetFile = new File(Path.of(ROOT_PATH.toString(), fileName).toUri());
            else
                targetFile = new File(fileName);

            if (!targetFile.exists()) {
                try (InputStream is = CoreConfigParser.class.getClassLoader().getResourceAsStream(fileName)) {
                    if (is != null) {
                        Files.copy(is, targetFile.toPath());
                        LOGGER.info("Copied default file {} to {}", fileName, targetFile);
                    } else
                        LOGGER.warn("Resource {} not found in classpath", fileName);
                } catch (Exception e) {
                    LOGGER.error("Could not extract content from {}. Error: {}", fileName, e.getMessage());
                }
            } else
                LOGGER.trace("File {} already exists, skipping copy", targetFile);
        }
    }

    /**
     * Returns an input stream for a configuration file, either from local path or resources
     *
     * @param fileName the filename
     * @return an input stream
     * @throws FileNotFoundException if the file cannot be found
     */
    private static InputStream getSmartStream(String fileName) throws FileNotFoundException {
        return getSmartStream(fileName, false);
    }

    /**
     * Returns an input stream for a configuration file, optionally from internal resources.
     *
     * @param fileName the filename
     * @param onlyInternal  if {@code true}, only internal resources are considered
     * @return an input stream
     * @throws FileNotFoundException if the file cannot be found
     */
    private static InputStream getSmartStream(String fileName, boolean onlyInternal) throws FileNotFoundException {
        if (!onlyInternal) {
            File localFile;
            if (ROOT_PATH != null)
                localFile = new File(Path.of(ROOT_PATH.toString(), fileName).toUri());
            else
                localFile = new File(fileName);

            if (localFile.exists()) {
                return new FileInputStream(localFile);
            }
        }

        InputStream is = getResourceStream(fileName);
        if (is == null) {
            throw new FileNotFoundException("Resource not found locally or in JAR: " + fileName);
        }
        return is;
    }

    /**
     * Loads the default core condition macros.
     *
     * @throws ConfigurationParseException if loading or parsing fails
     */
    private static void loadDefaultMacros() {
        LOGGER.debug("Loading default condition macros from {}", DEFAULT_CONDITION_MACROS_FILE);
        try (InputStream is = getResourceStream(DEFAULT_CONDITION_MACROS_FILE)) {
            if (is == null) throw new ConfigurationParseException(DEFAULT_CONDITION_MACROS_FILE + " is missing from internal resources!");
            
            Document doc = parseWithDtd(is, DTD_INSTRUCTION_SET);
            LOGGER.trace("Parsing default condition macros");
            MACRO_PARSER.parseConfig(doc);
            LOGGER.info("Default condition macros loaded successfully");
        } catch (Exception e) {
            final String errorMsg = "Failed to load default condition macros";
            LOGGER.error(errorMsg, e);
            throw new ConfigurationParseException(errorMsg, e);
        }
    }

    /**
     * Parses an input stream XML, validating against a DTD
     *
     * @param is the input stream
     * @param dtdName the DTD filename
     * @return the parsed {@link Document}
     * @throws Exception if parsing fails
     */
    private static Document parseWithDtd(InputStream is, String dtdName) throws Exception {
        LOGGER.trace("Parsing XML with DTD {}", dtdName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {}
            @Override
            public void error(SAXParseException e) throws SAXException { throw e; }
            @Override
            public void fatalError(SAXParseException e) throws SAXException { throw e; }
        });

        builder.setEntityResolver((_, systemId) -> {
            if (systemId != null && systemId.contains(dtdName)) {
                LOGGER.trace("Resolving DTD {} from internal resources", dtdName);
                return new InputSource(CoreConfigParser.class.getResourceAsStream("/" + dtdName));
            }
            LOGGER.trace("No DTD match for {}", systemId);
            return new InputSource(new StringReader(""));
        });

        final var doc = builder.parse(is);
        LOGGER.trace("XML parsing complete for DTD {}", dtdName);
        return doc;
    }

    /**
     * Returns an input stream from internal resources.
     *
     * @param name the resource name
     * @return an input stream, or {@code null} if resource not found
     */
    private static InputStream getResourceStream(String name) {
        return CoreConfigParser.class.getClassLoader().getResourceAsStream(name);
    }
}
