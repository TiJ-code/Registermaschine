package dk.tij.registermaschine.core.plugin;

import dk.tij.registermaschine.core.config.CoreConfigParser;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;

public final class PluginConfigParser {
    public static final PluginConfigParser INSTANCE = new PluginConfigParser();

    private PluginConfigParser() {}

    public PluginConfig parse(File pluginConfigFile)
            throws IOException, ParserConfigurationException, SAXException {
        try (InputStream is = Files.newInputStream(pluginConfigFile.toPath())) {
            Document doc = parseWithoutDtd(is);

            PluginConstants.FileVersion fv = null;

            String versionPlugin = doc.getDocumentElement().getAttribute(PluginConstants.ATTRIBUTE_PLUGIN_VERSION);
            if (!versionPlugin.isEmpty())
                fv = PluginConstants.FileVersion.match(versionPlugin);

            doc = parseWithDtd(is, fv);

            PluginConfig config;
            switch (fv) {
                case v1 -> config = parseV1(doc);
                case null, default -> config = parseV1(doc);
            }

            return config;
        }
    }

    private PluginConfig parseV1(Document xmlDocument) {
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

    private static Document parseWithDtd(InputStream is, PluginConstants.FileVersion fileVersion)
            throws IOException, ParserConfigurationException, SAXException {
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
            if (systemId != null) {
                return new InputSource(CoreConfigParser.class.getResourceAsStream("/" + fileVersion.getDtdPath()));
            }
            return new InputSource(new StringReader(""));
        });
        return builder.parse(is);
    }

    private static Document parseWithoutDtd(InputStream is)
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

    public static PluginConfigParser instance() {
        return INSTANCE;
    }
}
