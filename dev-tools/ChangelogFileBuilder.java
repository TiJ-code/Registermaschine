import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ChangelogFileBuilder {

    private static final File CHANGELOG_DIR = new File("changelogs");
    private static final File CHANGELOG_FILE = new File(CHANGELOG_DIR, "CHANGELOG.md");
    private static final File CHANGELOG_CURRENT_DIR = new File(CHANGELOG_DIR, "cumulated");
    private static final File CHANGELOG_ARCHIVE_DIR = new File(CHANGELOG_DIR, "archive");

    private static final String TAG_ROOT = "changelog";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_BREAKING = "breaking";
    private static final String TAG_ENTRY = "entry";

    private static final String ATTRIBUTE_CATEGORY = "name";
    private static final String ATTRIBUTE_BREAKING_KIND = "kind";

    // ---------------- ENTRY MODEL ----------------

    private enum EntryCategory {
        UNKNOWN(null, null),
        ADDITION("additions", "added %s"),
        REFACTORING("refactorings", "refactored %s"),
        FIX("fixes", "fixed %s"),
        REMOVAL("removals", "removed %s");

        final String xmlWrapper;
        final String mdPhrase;

        EntryCategory(String xml, String md) {
            this.xmlWrapper = xml;
            this.mdPhrase = md;
        }
    }

    private enum BreakingKind {
        TRIVIAL("trivial"),
        MINOR("minor"),
        MAJOR("major"),
        NONE(null);

        final String value;

        BreakingKind(String value) {
            this.value = value;
        }

        static BreakingKind from(String v) {
            for (var k : values()) {
                if (k.value != null && k.value.equals(v)) return k;
            }
            return NONE;
        }
    }

    private record XmlEntry(EntryCategory category, String text) {}

    private record BreakingGroup(BreakingKind kind, List<XmlEntry> entries) {}

    private record XmlCategory(
            String name,
            Map<EntryCategory, List<XmlEntry>> normal,
            List<BreakingGroup> breaking
    ) {}

    // ---------------- MAIN ----------------

    public static void main(String[] args) {
        File cumulationFile = getFirstFileInCumulation();
        List<XmlCategory> categories = parseXml(cumulationFile);

        String markdown = buildMarkdown(categories);
        File mdFile = resolveMarkdownFile(cumulationFile);

        try {
            Files.writeString(mdFile.toPath(), markdown);
            archiveFile(cumulationFile, mdFile);
            cumulationFile.deleteOnExit();
            moveMarkdownToRoot(mdFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- PARSING ----------------

    private static List<XmlCategory> parseXml(File file) {
        List<XmlCategory> categories = new ArrayList<>();

        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);

            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Element root = doc.getDocumentElement();

            if (!TAG_ROOT.equals(root.getTagName()))
                throw new ParseException("Invalid root", 0);

            var categoryNodes = root.getElementsByTagName(TAG_CATEGORY);

            for (int i = 0; i < categoryNodes.getLength(); i++) {
                Element catEl = (Element) categoryNodes.item(i);

                String name = catEl.getAttribute(ATTRIBUTE_CATEGORY);

                Map<EntryCategory, List<XmlEntry>> normal = parseNormal(catEl);
                List<BreakingGroup> breaking = parseBreaking(catEl);

                categories.add(new XmlCategory(name, normal, breaking));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }

    private static Map<EntryCategory, List<XmlEntry>> parseNormal(Element catEl) {
        Map<EntryCategory, List<XmlEntry>> map = new EnumMap<>(EntryCategory.class);

        for (EntryCategory cat : EntryCategory.values()) {
            if (cat == EntryCategory.UNKNOWN) continue;

            var wrappers = directChildren(catEl, cat.xmlWrapper);
            if (wrappers.isEmpty()) continue;

            List<XmlEntry> list = new ArrayList<>();

            for (Element w : wrappers) {
                var entries = w.getElementsByTagName(TAG_ENTRY);

                for (int i = 0; i < entries.getLength(); i++) {
                    Node n = entries.item(i);
                    if (n.getNodeType() != Node.ELEMENT_NODE) continue;

                    String text = n.getTextContent();
                    if (text != null && !text.isBlank())
                        list.add(new XmlEntry(cat, text.trim()));
                }
            }

            map.put(cat, list);
        }

        return map;
    }

    private static List<BreakingGroup> parseBreaking(Element catEl) {
        List<BreakingGroup> result = new ArrayList<>();

        var breakingNodes = directChildren(catEl, TAG_BREAKING);

        for (Element b : breakingNodes) {
            BreakingKind kind = BreakingKind.from(b.getAttribute(ATTRIBUTE_BREAKING_KIND));

            List<XmlEntry> entries = new ArrayList<>();

            for (EntryCategory cat : EntryCategory.values()) {
                if (cat == EntryCategory.UNKNOWN) continue;

                var wrappers = directChildren(b, cat.xmlWrapper);

                for (Element w : wrappers) {
                    var entryNodes = w.getElementsByTagName(TAG_ENTRY);

                    for (int i = 0; i < entryNodes.getLength(); i++) {
                        Node n = entryNodes.item(i);
                        if (n.getNodeType() != Node.ELEMENT_NODE) continue;

                        String text = n.getTextContent();
                        if (text != null && !text.isBlank())
                            entries.add(new XmlEntry(cat, text.trim()));
                    }
                }
            }

            result.add(new BreakingGroup(kind, entries));
        }

        return result;
    }

    // ---------------- MARKDOWN ----------------

    private static String buildMarkdown(List<XmlCategory> categories) {
        StringBuilder md = new StringBuilder();

        List<BreakingKind> breakingOrder = List.of(
                BreakingKind.TRIVIAL,
                BreakingKind.MINOR,
                BreakingKind.MAJOR
        );

        for (int i = 0; i < categories.size(); i++) {
            XmlCategory cat = categories.get(i);

            md.append("# ")
                    .append(cat.name())
                    .append("\n\n");

            // ---------------- NORMAL ENTRIES ----------------

            for (EntryCategory ec : EntryCategory.values()) {
                if (ec == EntryCategory.UNKNOWN)
                    continue;

                List<XmlEntry> entries =
                        cat.normal().getOrDefault(ec, List.of());

                for (XmlEntry entry : entries) {
                    md.append("- ")
                            .append(format(ec, entry.text()))
                            .append("\n");
                }
            }

            // ---------------- BREAKING CHANGES ----------------

            boolean hasBreaking = cat.breaking().stream()
                    .anyMatch(g -> !g.entries().isEmpty());

            if (hasBreaking) {
                md.append("\n")
                        .append("## Breaking Changes")
                        .append("\n\n");

                for (BreakingKind kind : breakingOrder) {

                    List<XmlEntry> entriesForKind = new ArrayList<>();

                    for (BreakingGroup group : cat.breaking()) {
                        if (group.kind() == kind) {
                            entriesForKind.addAll(group.entries());
                        }
                    }

                    if (entriesForKind.isEmpty())
                        continue;

                    md.append("### ")
                            .append(capitalize(kind.value))
                            .append("\n");

                    for (XmlEntry entry : entriesForKind) {
                        md.append("- ")
                                .append(format(entry.category(), entry.text()))
                                .append("\n");
                    }

                    md.append("\n");
                }
            }

            if (i != categories.size() - 1) {
                md.append("---\n\n");
            }
        }

        return md.toString();
    }

    private static String format(EntryCategory cat, String text) {
        return cat.mdPhrase == null ? text : cat.mdPhrase.formatted(text);
    }

    private static String capitalize(String str) {
        if (str == null || str.isBlank())
            return str;

        return Character.toUpperCase(str.charAt(0))
                + str.substring(1).toLowerCase();
    }

    // ---------------- HELPERS ----------------

    private static List<Element> directChildren(Element parent, String tag) {
        List<Element> out = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tag)) {
                out.add((Element) n);
            }
        }
        return out;
    }

    private static File getFirstFileInCumulation() {
        File[] files = CHANGELOG_CURRENT_DIR.listFiles();
        if (files == null || files.length == 0)
            throw new RuntimeException("No cumulative file");

        return files[0];
    }

    private static File resolveMarkdownFile(File xml) {
        return new File(xml.getParentFile(), xml.getName().replace(".xml", ".md"));
    }

    private static void archiveFile(File xml, File md) {
        try {
            if (!CHANGELOG_ARCHIVE_DIR.exists())
                CHANGELOG_ARCHIVE_DIR.mkdirs();

            File zip = new File(CHANGELOG_ARCHIVE_DIR,
                    xml.getName().replace(".xml", ".zip"));

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
                add(zos, xml);
                add(zos, md);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void add(ZipOutputStream zos, File f) throws Exception {
        zos.putNextEntry(new ZipEntry(f.getName()));
        try (FileInputStream fis = new FileInputStream(f)) {
            fis.transferTo(zos);
        }
        zos.closeEntry();
    }

    private static void moveMarkdownToRoot(File md) throws Exception {
        Files.move(md.toPath(), CHANGELOG_FILE.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }
}