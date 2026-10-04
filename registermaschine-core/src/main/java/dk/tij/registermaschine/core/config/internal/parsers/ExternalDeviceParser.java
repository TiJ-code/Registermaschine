package dk.tij.registermaschine.core.config.internal.parsers;

import dk.tij.registermaschine.api.config.IConfigParser;
import dk.tij.registermaschine.api.config.model.ConfigDevice;
import dk.tij.registermaschine.api.config.model.ConfigMemoryMapping;
import dk.tij.registermaschine.api.error.ConfigurationParseException;
import dk.tij.registermaschine.api.error.OutOfMemoryException;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.XmlConstants;
import dk.tij.registermaschine.core.devices.DeviceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser responsible for reading external device configuration from XML.
 *
 * <p>This parser interprets the <externalDevices> section of the configuration
 * file and converts it into runtime {@link ConfigDevice} and
 * {@link ConfigMemoryMapping} models.</p>
 *
 * <p>After parsing, all devices are immediately instantiated and registered
 * via {@link DeviceFactory} into the global memory system.</p>
 *
 * <p>The parser also performs validation such as:
 * <ul>
 *     <li>Required attributes</li>
 *     <li>Valid size units</li>
 *     <li>Memory bounds checking</li>
 *     <li>Single-child element constraints</li>
 * </ul>
 * </p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class ExternalDeviceParser implements IConfigParser {
    /**
     * Parses the external device configuration section from the XML document.
     *
     * <p>If no external device section is present, the method returns without
     * modifying the configuration.</p>
     *
     * @param xmlDocument the XML configuration document
     */
    @Override
    public void parseConfig(Document xmlDocument) {
        Element root = getSingleChildElement(xmlDocument.getDocumentElement(), XmlConstants.TAG_EXTERNAL_DEVICES, false);
        if (root == null)
            return;

        for (Element deviceEl : childElements(root, XmlConstants.TAG_EXTERNAL_DEVICE)) {
            CoreConfig.EXTERNAL_DEVICES.add(parseDevice(deviceEl));
        }

        DeviceFactory.createDevices(CoreConfig.EXTERNAL_DEVICES);
    }

    /**
     * Parses a single device element into a configuration model.
     *
     * @param deviceElement XML element describing a device
     * @return parsed device configuration
     */
    private static ConfigDevice parseDevice(Element deviceElement)
            throws ConfigurationParseException, OutOfMemoryException {
        String handler = requiredAttribute(deviceElement, XmlConstants.ATTRIBUTE_DEVICE_HANDLER);

        Element sizeEl = getSingleChildElement(deviceElement, XmlConstants.TAG_SIZE, true);
        Element mappingsEl = getSingleChildElement(deviceElement, XmlConstants.TAG_MEMORY_MAPPINGS, true);

        long deviceSize = parseSizeElement(sizeEl);
        List<ConfigMemoryMapping> mappings = parseMemoryMappings(mappingsEl);

        return new ConfigDevice(handler, deviceSize, mappings);
    }

    /**
     * Parses all memory mappings inside a device.
     *
     * @param mappingsElement XML element containing mapping definitions
     * @return list of parsed memory mappings
     */
    private static List<ConfigMemoryMapping> parseMemoryMappings(Element mappingsElement)
            throws ConfigurationParseException, OutOfMemoryException {
        List<ConfigMemoryMapping> mappings = new ArrayList<>();

        for (Element mappingEl : childElements(mappingsElement, XmlConstants.TAG_MEMORY_MAPPING)) {
            String handler = requiredAttribute(mappingEl, XmlConstants.ATTRIBUTE_MEMORY_MAPPING_HANDLER);

            Element sizeEl = getSingleChildElement(mappingEl, XmlConstants.TAG_SIZE, true);

            mappings.add(new ConfigMemoryMapping(handler, parseSizeElement(sizeEl)));
        }

        return mappings;
    }

    /**
     * Parses a <size> XML element into a raw byte size.
     *
     * <p>Supports unit conversion for:
     * <ul>
     *     <li>B</li>
     *     <li>KB</li>
     *     <li>MB</li>
     * </ul>
     * </p>
     *
     * @param sizeElement the XML size element
     * @return size in bytes
     */
    private static long parseSizeElement(Element sizeElement)
            throws ConfigurationParseException, OutOfMemoryException{
        final Map<String, Long> memorySizeMappings = Map.of(
                "B", 1L,
                "KB", 1024L,
                "MB", 1024L * 1024L
        );

        if (sizeElement == null) {
            throw new ConfigurationParseException("Expected <%s> element is actually null."
                    .formatted(XmlConstants.TAG_SIZE));
        }

        String unitStr = sizeElement.getAttribute(XmlConstants.ATTRIBUTE_SIZE_UNIT);

        Long sizeFactor = memorySizeMappings.get(unitStr);
        long size = parseSize(sizeElement, sizeFactor, unitStr);

        if (size == 0L) {
            throw new ConfigurationParseException("Size must not be 0.");
        }

        return size * sizeFactor;
    }

    /**
     * Parses the numeric size value and validates system memory constraints.
     *
     * <p>Also ensures that configured memory does not exceed available JVM memory.</p>
     *
     * @param sizeElement XML size element
     * @param sizeFactor unit multiplier
     * @param unitStr unit string (e.g. KB, MB)
     * @return parsed size in base units
     */
    private static long parseSize(Element sizeElement, Long sizeFactor, String unitStr)
            throws ConfigurationParseException, OutOfMemoryException{
        if (sizeFactor == null) {
            throw new ConfigurationParseException("Invalid unit type '%s' configured. Valid values are %s."
                    .formatted(unitStr, XmlConstants.SIZE_UNIT_VALUES));
        }

        String textContent = sizeElement.getTextContent();
        if (textContent.isEmpty()) {
            throw new ConfigurationParseException("Size must not be empty");
        }

        long size = 0L;
        try {
            size = Long.parseLong(textContent);
        } catch (NumberFormatException e) {
            throw new ConfigurationParseException("Size content '%s' is not an (long) integer."
                    .formatted(textContent));
        }

        long memorySize = size * sizeFactor;

        long jvmAvailableMemory = Runtime.getRuntime().freeMemory();
        if (memorySize >= jvmAvailableMemory) {
            throw new OutOfMemoryException(
                    "You cannot configure more memory in a <%s> tag, than available on your system. Available: %d B"
                    .formatted(XmlConstants.TAG_SIZE, jvmAvailableMemory));
        }

        return memorySize;
    }

    /**
     * Returns a single child element with the given tag name.
     *
     * @param parent parent XML element
     * @param tagName expected child tag name
     * @param required whether the element must exist
     * @return matching child element or null
     */
    private static Element getSingleChildElement(Element parent, String tagName, boolean required)
            throws ConfigurationParseException {
        List<Element> matches = childElements(parent, tagName);

        if (matches.isEmpty()) {
            if (required) {
                throw new ConfigurationParseException("<%s> must contain exactly one <%s>."
                        .formatted(parent.getNodeName(), tagName));
            }
            return null;
        }

        if (matches.size() > 1) {
            throw new ConfigurationParseException("<%s> must not contain more than one <%s>."
                    .formatted(parent.getNodeName(), tagName));
        }

        return matches.getFirst();
    }

    /**
     * Returns all direct child elements with the specified tag name.
     *
     * @param parent parent XML element
     * @param tagName tag name to search for
     * @return list of matching child elements
     */
    private static List<Element> childElements(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();

        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                result.add((Element) node);
            }
        }

        return result;
    }

    /**
     * Reads a required attribute from an XML element.
     *
     * @param element XML element
     * @param attrName attribute name
     * @return attribute value
     */
    private static String requiredAttribute(Element element, String attrName)
            throws ConfigurationParseException {
        String value = element.getAttribute(attrName);

        if (value.isEmpty()) {
            throw new ConfigurationParseException("<%s> attribute '%s' must not be empty."
                    .formatted(element.getNodeName(), attrName));
        }

        return value;
    }
}
