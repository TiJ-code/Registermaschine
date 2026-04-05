package dk.tij.registermaschine.core.plugin;

/**
 * @since 1.1.0
 * @author TiJ
 */
public final class PluginConstants {
    private PluginConstants() {}

    public enum FileVersion {
        v1("1", "dtd/plugin.dtd");

        private final String attributeVersion;
        private final String dtdPath;

        FileVersion(String attributeVersion, String dtdPath) {
            this.attributeVersion = attributeVersion;
            this.dtdPath = dtdPath;
        }

        public String getAttributeVersion() {
            return attributeVersion;
        }

        public String getDtdPath() {
            return dtdPath;
        }

        public static FileVersion match(String version) {
            var fvs = FileVersion.values();

            for (FileVersion v : fvs) {
                if (v.getAttributeVersion().equals(version))
                    return v;
            }
            return latest();
        }

        public static FileVersion latest() {
            return FileVersion.values()[0];
        }
    }

    public static final String PLUGIN_FILENAME = "plugin.xml";

    public static final String ATTRIBUTE_PLUGIN_VERSION = "version";

    public static final String  TAG_NAME = "name",
                                TAG_DESCRIPTION = "description",
                                TAG_VERSION = "version",
                                TAG_AUTHOR = "version",
                                TAG_MAIN = "main";
}
