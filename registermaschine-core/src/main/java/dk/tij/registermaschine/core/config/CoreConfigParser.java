package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.config.IConfigEventListener;
import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.core.config.internal.parsers.ConditionMacroParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionParser;
import dk.tij.registermaschine.core.config.internal.parsers.SettingsParser;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.instructions.IInstructionSet;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    /**
     * Name of the instruction parser target
     */
    public static final String  PARSER_INSTRUCTIONS = InstructionParser.class.getName();

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
    private static Path ROOT_PATH = null;

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
        if (coreConfigParsed) return;

        synchronized (INIT_LOCK) {
            if (coreConfigParsed) return;

            if (!onlyInternal) {
                copyDefaultFiles();
            }

            try (InputStream is = getSmartStream(CONFIGURATION_FILE, onlyInternal)) {
                Document doc = parseWithDtd(is, DTD_CONFIGURATION);

                INTERNAL_CONFIG_PARSERS.forEach(parser -> parser.parseConfig(doc));

                if (postConfigParsers != null) {
                    Arrays.stream(postConfigParsers)
                            .filter(Objects::nonNull)
                            .forEach(parser -> {
                                parser.parseConfig(doc);
                            });
                }

                coreConfigParsed = true;
            } catch (Exception e) {
                throw new ConfigurationParseException("Failed to initialise core configuration", e);
            }

            loadDefaultMacros();
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
            throw new IllegalStateException("Core Configuration must be initialised via init() before parsing instruction sets.");
        }

        try (InputStream is = getSmartStream(fileName)) {
            Document doc = parseWithDtd(is, DTD_INSTRUCTION_SET);

            CoreConfig.INSTRUCTIONS.clear();
            INSTRUCTION_PARSER.parseConfig(doc);
            MACRO_PARSER.parseConfig(doc);

            CoreConfig.INSTRUCTIONS.forEach(set::registerInstruction);
        } catch (Exception e) {
            throw new ConfigurationParseException("Failed to parse instruction set: " + fileName, e);
        }
    }

    /**
     * Adds a configuration event listener to a specific parser target.
     *
     * @param target the parser target name
     * @param listener the listener to register
     */
    public static void addListenerToTarget(String target, IConfigEventListener listener) {
        if (PARSER_INSTRUCTIONS.equals(target))
            INSTRUCTION_PARSER.addListener(listener);
    }

    /**
     * Sets a custom root path for user-editable configuration files.
     *
     * @param customPath the root path
     */
    public static void setCustomRootPath(Path customPath) {
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
                    if (is != null)
                        Files.copy(is, targetFile.toPath());
                } catch (Exception e) {
                    System.err.printf("Warning: Could not extract %s: %s%n", fileName, e.getMessage());
                }
            }
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
        try (InputStream is = getResourceStream(DEFAULT_CONDITION_MACROS_FILE)) {
            if (is == null) throw new ConfigurationParseException(DEFAULT_CONDITION_MACROS_FILE + " is missing from internal resources!");
            
            Document doc = parseWithDtd(is, DTD_INSTRUCTION_SET);
            MACRO_PARSER.parseConfig(doc);
        } catch (Exception e) {
            throw new ConfigurationParseException("Critical failure loading core condition macros", e);
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
                return new InputSource(CoreConfigParser.class.getResourceAsStream("/" + dtdName));
            }
            return new InputSource(new StringReader(""));
        });
        return builder.parse(is);
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
