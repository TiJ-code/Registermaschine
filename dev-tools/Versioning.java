import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

public final class Versioning {
    private static final String BUMP_MAJOR = "major";
    private static final String BUMP_MINOR = "minor";
    private static final String BUMP_PATCH = "patch";
    private static final String BUMP_DO_SUFFIX = "do_suffix";

    private static final String[] BUMPS = { BUMP_MAJOR, BUMP_MINOR, BUMP_PATCH, BUMP_DO_SUFFIX };

    private Versioning() {}

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Version bump type has to be supplied!");
            System.err.printf("Possibilities are: %s%n", Arrays.toString(BUMPS));
            System.exit(1);
        }

        String originalVersionStr = fetchVersionFromPom();
        if (originalVersionStr == null || originalVersionStr.isBlank()) {
            System.err.println("Version string not readable or blank");
            System.exit(1);
        }

        String versionStr = originalVersionStr;
        int suffixIdx = originalVersionStr.indexOf('-');
        if (suffixIdx != -1) {
            versionStr = originalVersionStr.substring(0, suffixIdx);
        }

        String[] parts = versionStr.split("\\.");
        if (parts.length != 3) {
            System.err.println("Version has unexpected format. Requires major.minor.patch");
            System.exit(1);
        }

        int major = 0, minor = 0, patch = 0;
        try {
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
            patch = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            System.err.println("Cannot parse version.");
            System.exit(1);
        }

        switch (args[0]) {
            case BUMP_MAJOR -> {
                ++major; minor = 0; patch = 0;
            }
            case BUMP_MINOR -> {
                ++minor; patch = 0;
            }
            case BUMP_PATCH -> {
                ++patch;
            }
        }

        String targetVersion = "%d.%d.%d".formatted(major, minor, patch);
        if (BUMP_DO_SUFFIX.equals(args[0])) {
            targetVersion = "%s-SNAPSHOT".formatted(targetVersion);
        }

        writeVersionToPom(originalVersionStr, targetVersion);
    }

    private static void writeVersionToPom(String oldStr, String newStr) {
        try {
            File rootDir = new File(".").getCanonicalFile();

            Files.walk(rootDir.toPath())
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .forEach(path -> updatePom(path.toFile(), oldStr, newStr));

        } catch (Exception e) {
            System.err.println("Writing to pom.xml files failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void updatePom(File pomFile, String oldVersion, String newVersion) {
        try {
            String fileString = Files.readString(pomFile.toPath());
            fileString = fileString.replace(oldVersion, newVersion);
            Files.writeString(pomFile.toPath(), fileString);
        } catch (Exception e) {
            System.err.println("Failed updating " + pomFile.getPath());
            e.printStackTrace();
        }
    }

    private static String fetchVersionFromPom() {
        try {
            File dir = new File(".").getCanonicalFile();
            while (dir != null) {
                File candidate = new File(dir, "pom.xml");
                if (candidate.exists() && candidate.isFile()) {
                    try {
                        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = builder.parse(candidate);
                        var root = doc.getDocumentElement();
                        if (root == null)
                            continue;

                        if ("project".equals(root.getTagName()) || "project".equalsIgnoreCase(root.getLocalName())) {
                            var children = root.getChildNodes();
                            for (int i = 0; i < children.getLength(); i++) {
                                var node = children.item(i);
                                if (node.getNodeType() != Node.ELEMENT_NODE)
                                    continue;
                                var el = (Element) node;
                                if ("version".equals(el.getTagName()) || "version".equalsIgnoreCase(el.getLocalName())) {
                                    var text = el.getTextContent();
                                    if (text != null && !text.isBlank())
                                        return text.trim();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dir = dir.getParentFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
