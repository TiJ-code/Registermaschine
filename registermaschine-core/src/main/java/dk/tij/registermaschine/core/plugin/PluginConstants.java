package dk.tij.registermaschine.core.plugin;

/**
 * Contains constants and version definitions used by the plugin system.
 *
 * <p>This class stores XML tag names, attribute names,
 * plugin descriptor file names, and supported plugin
 * configuration file versions.</p>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class PluginConstants {
    /**
     * Private constructor to prevent instantiation
     */
    private PluginConstants() {}

    /**
     * Represents supported plugin configuration file versions.
     *
     * <p>Each version defines the XML version attribute value
     * and the corresponding DTD resource path used for validation.</p>
     */
    public enum FileVersion {
        /**
         * Plugin configuration format version 1.
         */
        v1("1", "dtd/plugin.dtd");

        /**
         * Version string stored in the XML {@code version} attribute.
         */
        private final String attributeVersion;

        /**
         * Path to the DTD resource used for XML validation.
         */
        private final String dtdPath;

        /**
         * Creates a new file version definition.
         *
         * @param attributeVersion XML version attribute value
         * @param dtdPath path to the associated DTD resource
         */
        FileVersion(String attributeVersion, String dtdPath) {
            this.attributeVersion = attributeVersion;
            this.dtdPath = dtdPath;
        }

        /**
         * Returns the XML attribute version string.
         *
         * @return XML version value
         */
        public String getAttributeVersion() {
            return attributeVersion;
        }

        /**
         * Returns the path to the DTD resource.
         *
         * @return DTD resource path
         */
        public String getDtdPath() {
            return dtdPath;
        }

        /**
         * Matches a version string to a supported file version.
         *
         * <p>If no matching version exists, the latest version
         * is returned.</p>
         *
         * @param version  version string from the XML file
         * @return matching file version, or latest
         */
        public static FileVersion match(String version) {
            var fvs = FileVersion.values();

            for (FileVersion v : fvs) {
                if (v.getAttributeVersion().equals(version))
                    return v;
            }
            return latest();
        }

        /**
         * Returns the latest supported plugin file version.
         *
         * @return latest file version
         */
        public static FileVersion latest() {
            return FileVersion.values()[0];
        }
    }

    /**
     * Name of the plugin descriptor file inside plugin JARs.
     */
    public static final String PLUGIN_FILENAME = "plugin.xml";

    /**
     * XML attribute name used for the plugin configuration version.
     */
    public static final String ATTRIBUTE_PLUGIN_VERSION = "version";

    /**
     * XML tag name for the plugin name.
     */
    public static final String  TAG_NAME = "name",
    /**
     * XML tag name for the plugin description.
     */
                                TAG_DESCRIPTION = "description",
    /**
     * XML tag name for the plugin version.
     */
                                TAG_VERSION = "version",
    /**
     * XML tag name for the plugin author.
     */
                                TAG_AUTHOR = "version",
    /**
     * XML tag name for the plugin main class.
     */
                                TAG_MAIN = "main";
}
