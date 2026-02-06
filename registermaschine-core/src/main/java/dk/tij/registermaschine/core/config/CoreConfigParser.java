package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.parsers.ConditionMacroParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionParser;
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
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class CoreConfigParser {
    public static final String  DTD_CONFIGURATION = "dtd/configuration.dtd",
                                DTD_INSTRUCTION_SET = "dtd/instruction_file.dtd";

    private static final String CONFIGURATION_FILE = "configuration.jxml",
                                DEFAULT_CONDITION_MACROS_FILE = "core_condition_macros.jxml",
                                DEFAULT_INSTRUCTION_SET_FILE = "default.instructions.jxml";

    private static volatile boolean coreConfigParsed = false;
    private static final Object INIT_LOCK = new Object();

    private static final List<IConfigParser> INTERNAL_CONFIG_PARSERS = List.of(
            new SettingsParser()
    );
    private static final IConfigParser MACRO_PARSER = new ConditionMacroParser();
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

    public static void parseInstructionSet(IInstructionSet set) throws ConfigurationParseException {
        parseInstructionSet(DEFAULT_INSTRUCTION_SET_FILE, set);
    }

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

    private static void copyDefaultFiles() {
        List<String> userEditableFiles = List.of(
                CONFIGURATION_FILE, DEFAULT_INSTRUCTION_SET_FILE
        );

        for (String fileName : userEditableFiles) {
            File targetFile = new File(fileName);

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

    private static InputStream getSmartStream(String fileName) throws FileNotFoundException {
        return getSmartStream(fileName, false);
    }

    private static InputStream getSmartStream(String fileName, boolean onlyInternal) throws FileNotFoundException {
        if (!onlyInternal) {
            File localFile = new File(fileName);
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
            
            Document doc = parseWithDtd(is, DTD_INSTRUCTION_SET);
            MACRO_PARSER.parseConfig(doc);
        } catch (Exception e) {
            throw new ConfigurationParseException("Critical failure loading core condition macros", e);
        }
    }

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

    private static InputStream getResourceStream(String name) {
        return CoreConfigParser.class.getClassLoader().getResourceAsStream(name);
    }
}
