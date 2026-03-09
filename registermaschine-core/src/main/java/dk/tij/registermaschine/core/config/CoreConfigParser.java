package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.config.api.IConfigEventListener;
import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.migration.InstructionSetMigrator;
import dk.tij.registermaschine.core.config.internal.parsers.ConditionMacroParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionSetOptionParser;
import dk.tij.registermaschine.core.config.internal.parsers.SettingsParser;
import dk.tij.registermaschine.core.error.ConfigurationParseException;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class CoreConfigParser {
    public static final String  PARSER_INSTRUCTIONS = InstructionParser.class.getName();
    public static final String  PARSER_INSTRUCTION_OPTIONS = InstructionSetOptionParser.class.getName();

    public static final String  DTD_CONFIGURATION = "dtd/configuration.dtd",
                                DTD_INSTRUCTION_SET = "dtd/instruction_file.dtd",
                                DTD_DIRECTORY = "dtd";

    private static final String CONFIGURATION_FILE = "configuration.jxml",
                                DEFAULT_CONDITION_MACROS_FILE = "core_condition_macros.jxml",
                                DEFAULT_INSTRUCTION_SET_FILE = "default.instructions.jxml";

    private static Path ROOT_PATH = null;

    private static volatile boolean coreConfigParsed = false;
    private static final Object INIT_LOCK = new Object();

    private static final List<IConfigParser> INTERNAL_CONFIG_PARSERS = List.of(
            new SettingsParser()
    );
    private static final IConfigParser MACRO_PARSER = new ConditionMacroParser();
    private static final IConfigParser INSTRUCTION_OPTIONS_PARSER = new InstructionSetOptionParser();
    private static final IConfigParser INSTRUCTION_PARSER = new InstructionParser();

    private CoreConfigParser() {}

    public static void init(IConfigParser... postConfigParsers) throws ConfigurationParseException {
        init(false, postConfigParsers);
    }

    public static void init(boolean onlyInternal, IConfigParser... postConfigParsers) {
        if (coreConfigParsed) return;

        synchronized (INIT_LOCK) {
            if (coreConfigParsed) return;

            if (!onlyInternal) {
                copyDefaultFiles();
            }

            try (InputStream is = getSmartStream(CONFIGURATION_FILE, onlyInternal)) {
                Document doc = parseWithDtd(is);

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

    public static void parseDefaultInstructionSet(IInstructionSet set) throws ConfigurationParseException {
        parseInstructionSet(DEFAULT_INSTRUCTION_SET_FILE, set);
    }

    public static void parseInstructionSet(String fileName, IInstructionSet set)
            throws ConfigurationParseException {
        if (!coreConfigParsed) {
            throw new IllegalStateException("Core Configuration must be initialised via init() before parsing instruction sets.");
        }

        try {
            Path filePath = resolvePath(fileName);

            try (InputStream is = Files.newInputStream(filePath)) {
                Document doc = parseWithoutValidation(is);

                InstructionSetMigrator.migrateAndStoreIfNeeded(doc, filePath);

                validateWithDtd(doc);

                CoreConfig.INSTRUCTIONS.clear();
                INSTRUCTION_OPTIONS_PARSER.parseConfig(doc);
                INSTRUCTION_PARSER.parseConfig(doc);
                MACRO_PARSER.parseConfig(doc);

                CoreConfig.INSTRUCTIONS.forEach(set::registerInstruction);
            }
        } catch (Exception e) {
            throw new ConfigurationParseException("Failed to parse instruction set: " + fileName, e);
        }
    }

    public static void addListenerToTarget(String target, IConfigEventListener listener) {
        if (PARSER_INSTRUCTIONS.equals(target))
            INSTRUCTION_PARSER.addListener(listener);
        if (PARSER_INSTRUCTION_OPTIONS.equals(target))
            INSTRUCTION_OPTIONS_PARSER.addListener(listener);
    }

    public static void setCustomRootPath(Path customPath) {
        ROOT_PATH = customPath;
    }

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

    private static Path resolvePath(String fileName) throws IOException {
        if (ROOT_PATH != null) {
            Path path = ROOT_PATH.resolve(fileName);
            if (Files.exists(path)) return path;
        }

        Path local = Path.of(fileName);
        if (Files.exists(local)) return local;

        throw new FileNotFoundException("Instruction file must exist on disk for migration: " + fileName);
    }

    private static InputStream getSmartStream(String fileName) throws FileNotFoundException {
        return getSmartStream(fileName, false);
    }

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

    private static void loadDefaultMacros() {
        try (InputStream is = getResourceStream(DEFAULT_CONDITION_MACROS_FILE)) {
            if (is == null) throw new ConfigurationParseException(DEFAULT_CONDITION_MACROS_FILE + " is missing from internal resources!");
            
            Document doc = parseWithDtd(is);
            MACRO_PARSER.parseConfig(doc);
        } catch (Exception e) {
            throw new ConfigurationParseException("Error loading resource", e);
        }
    }

    private static Document parseWithoutValidation(InputStream is)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(false);

        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/validation/dynamic", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    private static void validateWithDtd(Document doc)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DTD_INSTRUCTION_SET);

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        String xml = writer.toString();

        parseWithDtd(new ByteArrayInputStream(xml.getBytes()));
    }

    private static Document parseWithDtd(InputStream is)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override public void warning(SAXParseException e) throws SAXException {}
            @Override
            public void error(SAXParseException e) throws SAXException { throw e; }
            @Override
            public void fatalError(SAXParseException e) throws SAXException { throw e; }
        });

        builder.setEntityResolver((_, systemId) -> {
            if (systemId != null) {
                String dtdFile = systemId.substring(systemId.lastIndexOf('/') + 1);
                return new InputSource(CoreConfigParser.class.getResourceAsStream(
                        String.format("/%s/%s", DTD_DIRECTORY, dtdFile))
                );
            }
            return new InputSource(new StringReader(""));
        });
        return builder.parse(is);
    }

    private static InputStream getResourceStream(String name) {
        return CoreConfigParser.class.getClassLoader().getResourceAsStream(name);
    }
}
