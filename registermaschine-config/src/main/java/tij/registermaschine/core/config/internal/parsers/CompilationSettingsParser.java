package tij.registermaschine.core.config.internal.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tij.registermaschine.api.config.IConfigParser;
import tij.registermaschine.api.log.ILogger;
import tij.registermaschine.api.log.LoggerFactory;
import tij.registermaschine.core.config.CoreConfig;
import tij.registermaschine.core.config.internal.XmlConstants;

/**
 * Parses the factory identifiers used by the default compilation pipeline.
 *
 * <p>The parser stores identifiers rather than resolving factories. Component
 * registries are populated later during core and plugin startup, so resolution
 * is intentionally deferred until a compilation is requested.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class CompilationSettingsParser implements IConfigParser {
    private static final ILogger log = LoggerFactory.getLogger(CompilationSettingsParser.class);

    /**
     * Parses the optional compilation settings section.
     *
     * @param xmlDocument the configuration document
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        log.info("Started parsing compilation settings");

        NodeList compilationNodes = xmlDocument.getElementsByTagName(XmlConstants.TAG_CONFIG_COMPILATION);
        if (compilationNodes.getLength() == 0) {
            log.debug("No <{}> element found; retaining default compilation factories",
                    XmlConstants.TAG_CONFIG_COMPILATION);
            return;
        }

        Element compilation = (Element) compilationNodes.item(0);
        CoreConfig.LEXER_FACTORY = parseFactoryId(
                compilation, XmlConstants.TAG_CONFIG_LEXER, CoreConfig.LEXER_FACTORY);
        CoreConfig.PARSER_FACTORY = parseFactoryId(
                compilation, XmlConstants.TAG_CONFIG_PARSER, CoreConfig.PARSER_FACTORY);
        CoreConfig.COMPILER_FACTORY = parseFactoryId(
                compilation, XmlConstants.TAG_CONFIG_COMPILER, CoreConfig.COMPILER_FACTORY);
        CoreConfig.TOKEN_FACTORY = parseFactoryId(
                compilation, XmlConstants.TAG_CONFIG_TOKEN_FACTORY, CoreConfig.TOKEN_FACTORY);
        CoreConfig.COMPILED_INSTRUCTION_FACTORY = parseFactoryId(
                compilation,
                XmlConstants.TAG_CONFIG_COMPILED_INSTRUCTION_FACTORY,
                CoreConfig.COMPILED_INSTRUCTION_FACTORY
        );

        log.info(
                "Parsed compilation factories: lexer={}, parser={}, compiler={}, token={}, compiledInstruction={}",
                CoreConfig.LEXER_FACTORY,
                CoreConfig.PARSER_FACTORY,
                CoreConfig.COMPILER_FACTORY,
                CoreConfig.TOKEN_FACTORY,
                CoreConfig.COMPILED_INSTRUCTION_FACTORY
        );
    }

    /**
     * Reads a factory identifier from a compilation setting.
     *
     * @param parent parent compilation element
     * @param tagName setting element name
     * @param defaultValue value used when the setting is absent
     * @return the configured factory identifier
     */
    private static String parseFactoryId(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            log.debug("No <{}> element found; retaining {}", tagName, defaultValue);
            return defaultValue;
        }

        String value = ((Element) nodes.item(0))
                .getAttribute(XmlConstants.ATTRIBUTE_FACTORY_VALUE)
                .trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Compilation factory ID cannot be empty: " + tagName);
        }

        return value;
    }
}
