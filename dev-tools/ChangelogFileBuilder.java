import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ChangelogFileBuilder {
    private static final File CHANGELOG_DIR = new File("changelogs");
    private static final File CHANGELOG_FILE= new File(CHANGELOG_DIR, "CHANGELOG.md");
    private static final File CHANGELOG_CURRENT_DIR = new File(CHANGELOG_DIR, "cumulated");
    private static final File CHANGELOG_ARCHIVE_DIR = new File(CHANGELOG_DIR, "archive");

    private static final String MD_HEADING_1 = "#";
    private static final String MD_HEADING_2 = "##";
    private static final String MD_HEADING_3 = "###";
    private static final String MD_HEADING_4 = "####";
    private static final String MD_SEPARATOR = "---";

    private static final String TAG_ROOT = "changelog";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_ENTRY = "entry";

    private static final String ATTRIBUTE_CATEGORY = "name";

    private ChangelogFileBuilder() {}

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

    private static File getFirstFileInCumulation() {
        File[] files = CHANGELOG_CURRENT_DIR.listFiles();

        if (files == null)
            throw new RuntimeException("No file in cumulative dir!");

        if (files.length > 1)
            System.err.println("Too many files in cumulative dir, there should only be 1! Taking first deleting others");

        for (int i = 1; i < files.length; i++) {
            try {
                Files.deleteIfExists(files[i].toPath());
            } catch (Exception e) {
                throw new RuntimeException("Cannot delete useless file!");
            }
        }

        return files[0];
    }

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
                throw new ParseException("Root tag is not <%s>".formatted(TAG_ROOT), 0);

            var categoryNodes = root.getElementsByTagName(TAG_CATEGORY);

            for (int i = 0; i < categoryNodes.getLength(); i++) {
                var node = categoryNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                var catEl = (Element) node;

                String categoryName = catEl.getAttribute(ATTRIBUTE_CATEGORY);

                List<XmlEntry> entries = parseCategoryEntries(catEl);

                categories.add(new XmlCategory(categoryName, entries));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }

    private static List<XmlEntry> parseCategoryEntries(Element catEl) {
        List<XmlEntry> result = new ArrayList<>();

        for (EntryCategory category : EntryCategory.values()) {
            if (category == EntryCategory.UNKNOWN)
                continue;

            var wrappers = catEl.getElementsByTagName(category.xmlWrapper);

            if (wrappers.getLength() == 0)
                continue;

            var wrapper = (Element) wrappers.item(0);

            var entryNodes = wrapper.getElementsByTagName(TAG_ENTRY);

            List<String> entries = new ArrayList<>();

            for (int i = 0; i < entryNodes.getLength(); i++) {
                var node = entryNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                String text = node.getTextContent();

                if (text == null || text.isBlank())
                    continue;

                entries.add(text.trim());
            }

            result.add(new XmlEntry(category, entries));
        }

        return result;
    }

    private static String buildMarkdown(List<XmlCategory> categories) {
        StringBuilder md = new StringBuilder();

        final int lastCatIdx = categories.size() - 1;
        for (int i = 0; i < categories.size(); i++) {
            var category = categories.get(i);

            md.append(MD_HEADING_1)
                    .append(" ")
                    .append(category.name())
                    .append("\n\n");

            for (var entryGroup : category.entries()) {
                if (entryGroup.entries().isEmpty())
                    continue;


                for (var entry : entryGroup.entries()) {
                    md.append("- ")
                            .append(formatEntry(entryGroup.category(), entry))
                            .append("\n");
                }

                md.append("\n");
            }

            if (i != lastCatIdx)
                md.append(MD_SEPARATOR);

            md.append("\n\n");
        }

        return md.toString();
    }

    private static File resolveMarkdownFile(File cumulationFile) {
        String name = cumulationFile.getName();

        int dotIdx = name.lastIndexOf('.');

        if (dotIdx != -1)
            name = name.substring(0, dotIdx);

        name += ".md";

        return new File(cumulationFile.getParentFile(), name);
    }

    private static void archiveFile(File xmlFile, File mdFile) {
        try {
            if (!CHANGELOG_ARCHIVE_DIR.exists()) {
                if (!CHANGELOG_ARCHIVE_DIR.mkdirs())
                    throw new RuntimeException("Cannot create archive dir!");
            }

            String archiveName = xmlFile.getName();
            int dotIdx = archiveName.lastIndexOf('.');
            if (dotIdx != -1)
                archiveName = archiveName.substring(0, dotIdx);

            archiveName += ".zip";

            File archiveFile = new File(CHANGELOG_ARCHIVE_DIR, archiveName);

            try (var fos = new FileOutputStream(archiveFile);
                 var zos = new ZipOutputStream(fos)) {
                addFileZoZip(xmlFile, zos);
                addFileZoZip(mdFile, zos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFileZoZip(File file, ZipOutputStream zos) throws Exception {
        try (var fis = new FileInputStream(file)) {
            var entry = new ZipEntry(file.getName());

            zos.putNextEntry(entry);

            byte[] buffer = new byte[8192];

            int len;
            while ((len = fis.read(buffer)) > 0)
                zos.write(buffer, 0, len);

            zos.closeEntry();
        }
    }

    private static void moveMarkdownToRoot(File mdFile) {
        try {
            Files.move(mdFile.toPath(), CHANGELOG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formatEntry(EntryCategory category, String text) {
        if (category.mdPhrase == null)
            return text;

        return category.mdPhrase.formatted(text);
    }

    private enum EntryCategory {
        UNKNOWN(null, null),
        ADDITION("additions", "added %s"),
        REFACTORING("refactorings", "refactored %s"),
        FIX("fixes", "fixed %s"),
        REMOVAL("removals", "removed %s");

        public final String xmlWrapper;
        public final String mdPhrase;

        EntryCategory(String xml, String md) {
            this.xmlWrapper = xml;
            this.mdPhrase = md;
        }
    }

    private record XmlCategory(String name, List<XmlEntry> entries) {}

    private record XmlEntry(EntryCategory category, List<String> entries) {}
}
