package dk.tij.registermaschine.core.plugin;

import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.CoreConfigParser;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses plugin configuration files ({@link PluginConstants#PLUGIN_FILENAME})
 * into {@link PluginConfig} instances.
 *
 * <p>The parser supports versioned plugin configuration formats and validates
 * XML documents against their corresponding DTDs.</p>
 *
 * <p>Parsing is performed in two steps</p>
 * <ol>
 *     <li>Initial parsing without DTD validation to determine file version</li>
 *     <li>Validation and parsing using the correct DTD</li>
 * </ol>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class PluginConfigParser {
    private static final ILogger LOGGER = LoggerFactory.getLogger(PluginConfigParser.class);

    /**
     * Singleton parser instance.
     */
    private static final PluginConfigParser INSTANCE = new PluginConfigParser();

    /**
     * Default plugin directory path.
     */
    public static final Path PLUGIN_PATH = Path.of("plugins");

    /**
     * Private constructor to prevent external instantiation
     */
    private PluginConfigParser() {}

    /**
     * Returns the plugin directory path.
     *
     * <p>The directory is automatically created if it does not exist.</p>
     *
     * @return plugin directory path
     */
    public Path getPluginDirectory() {
        try {
            Files.createDirectories(PLUGIN_PATH);
            return PLUGIN_PATH;
        } catch (IOException e) {
            throw new ConfigurationParseException("Cannot create directory: " + PLUGIN_PATH, e);
        }
    }

    /**
     * Parses and validates an XML document using specified DTD.
     *
     * @param is input stream containing the XML document
     * @param fileVersion plugin file version used to determine the DTD
     * @return parsed XML document
     *
     * @throws IOException if the input stream cannot be read
     * @throws ParserConfigurationException if the XML parser configuration fails
     * @throws SAXException if XML validation fails
     */
    private static Document parseWithDtd(InputStream is, PluginConstants.FileVersion fileVersion)
            throws IOException, ParserConfigurationException, SAXException {
        LOGGER.trace("Parsing input stream with DTD");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override public void warning(SAXParseException e) throws SAXException {}

            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });

        builder.setEntityResolver((_, systemId) -> {
            if (systemId != null) {
                return new InputSource(CoreConfigParser.class.getResourceAsStream("/" + fileVersion.getDtdPath()));
            }
            return new InputSource(new StringReader(""));
        });
        return builder.parse(is);
    }

    /**
     * Parses an XML document without DTD validation.
     *
     * <p>External DTD loading and entity processing are disabled
     * to avoid unnecessary network access and security risks.</p>
     *
     * @param is input stream containing the XML document
     * @return parsed XML document
     *
     * @throws IOException if the input stream cannot be read
     * @throws ParserConfigurationException if the XML parser configuration fails
     * @throws SAXException if parsing fails
     */
    private static Document parseWithoutDtd(InputStream is)
            throws IOException, ParserConfigurationException, SAXException {
        LOGGER.trace("Parsing input stream without DTD");
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

    /**
     * Returns the singleton parser instance.
     *
     * @return parser instance
     */
    public static PluginConfigParser instance() {
        return INSTANCE;
    }

    /**
     * Parses a plugin configuration XML document.
     *
     * <p>The parser first determines the plugin file version
     * and then validates the XML document using the correct DTD.</p>
     *
     * @param is input stream containing the plugin configuration XML
     * @return parsed plugin configuration
     * @throws IOException if the input stream cannot be read
     * @throws ParserConfigurationException if the XML parser configuration fails
     * @throws SAXException if XML aprsing or validation fails
     */
    public PluginConfig parse(InputStream is)
            throws IOException, ParserConfigurationException, SAXException {
        LOGGER.trace("Begin parsing of plugin configuration file");
        byte[] data = is.readAllBytes();

        Document doc = parseWithoutDtd(new ByteArrayInputStream(data));

        PluginConstants.FileVersion fv = null;

        String versionPlugin = doc.getDocumentElement().getAttribute(PluginConstants.ATTRIBUTE_PLUGIN_VERSION);
        if (!versionPlugin.isEmpty())
            fv = PluginConstants.FileVersion.match(versionPlugin);

        doc = parseWithDtd(new ByteArrayInputStream(data), fv);

        PluginConfig config;
        switch (fv) {
            case v1 -> config = parseV1(doc);
            case null, default -> config = parseV1(doc);
        }

        LOGGER.trace("Done parsing of plugin configuration file");
        return config;
    }

    /**
     * Parses a version 1 plugin configuration document.
     *
     * @param xmlDocument parsed XML document
     * @return parsed plugin configuration
     */
    private PluginConfig parseV1(Document xmlDocument) {
        LOGGER.trace("Parsing plugin configuration of version 1");
        String nameStr = xmlDocument.getElementsByTagName(PluginConstants.TAG_NAME)
                .item(0).getTextContent();
        String descriptionStr = xmlDocument.getElementsByTagName(PluginConstants.TAG_DESCRIPTION)
                .item(0).getTextContent();
        String versionStr = xmlDocument.getElementsByTagName(PluginConstants.TAG_VERSION)
                .item(0).getTextContent();
        String authorStr = xmlDocument.getElementsByTagName(PluginConstants.TAG_AUTHOR)
                .item(0).getTextContent();
        String mainStr = xmlDocument.getElementsByTagName(PluginConstants.TAG_MAIN)
                .item(0).getTextContent();

        return new PluginConfig(nameStr, descriptionStr, versionStr, authorStr, mainStr);
    }
}
