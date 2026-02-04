package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.config.api.IConfigParser;
import dk.tij.registermaschine.core.config.internal.parsers.ConditionMacroParser;
import dk.tij.registermaschine.core.config.internal.parsers.InstructionParser;
import dk.tij.registermaschine.core.config.internal.parsers.RegisterParser;
import dk.tij.registermaschine.core.error.ConfigurationParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public final class CoreConfigParser {
    private CoreConfigParser() {}

    private static final List<IConfigParser> internalConfigParsers = List.of(
            new RegisterParser(),
            new ConditionMacroParser(),
            new InstructionParser()
    );

    public static void parseCoreConfig(InstructionSet set, IConfigParser... customConfigParsers) throws ConfigurationParseException {
        try (InputStream is = CoreConfigParser.class.getClassLoader().getResourceAsStream("configuration.jxml")) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    if (systemId.contains("configuration.dtd")) {
                        return new InputSource(getClass().getResourceAsStream("/configuration.dtd"));
                    }
                    return null;
                }
            });
            Document doc = builder.parse(is);

            internalConfigParsers.forEach(parser -> {
                parser.parseConfig(doc);
            });

            if (customConfigParsers != null) {
                Arrays.stream(customConfigParsers)
                        .filter(Objects::nonNull)
                        .forEach(parser -> {
                    parser.parseConfig(doc);
                });
            }
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }
}
