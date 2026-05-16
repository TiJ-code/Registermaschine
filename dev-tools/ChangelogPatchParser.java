import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ChangelogPatchParser {
    private static final File CHANGELOG_DIR = new File("changelogs");
    private static final File CHANGELOG_CUMULATED_DIR = new File(CHANGELOG_DIR, "cumulated");
    private static final File CHANGELOG_PATCHES_DIR = new File(CHANGELOG_DIR, "patches");

    private static final String TAG_PATCH_ROOT = "patch";
    private static final String TAG_CHANGELOG_ROOT = "changelog";

    private static final String TAG_CATEGORY = "category";
    private static final String TAG_BREAKING = "breaking";
    private static final String TAG_ADDITIONS = "additions";
    private static final String TAG_REFACTORED = "refactored";
    private static final String TAG_FIXED = "fixes";
    private static final String TAG_REMOVALS = "removals";
    private static final String TAG_ENTRY = "entry";

    private static final String ATTRIBUTE_CATEGORY = "name";
    private static final String ATTRIBUTE_BREAKING_KIND = "kind";
    private static final String ATTRIBUTE_VERSION = "version";

    private static final List<XmlEntry> additionsList = new ArrayList<>();
    private static final List<XmlEntry> refactoringsList = new ArrayList<>();
    private static final List<XmlEntry> fixesList = new ArrayList<>();
    private static final List<XmlEntry> removalsList = new ArrayList<>();

    private ChangelogPatchParser() {}

    public static void main(String[] args) {
        List<String> xmlFiles = fetchAndValidatePatchFiles();

        if (xmlFiles.isEmpty())
            return;

        String versionStr = fetchVersionFromPom();

        fetchByPatches(xmlFiles);
        File resultFile = cumulatePatches(versionStr);

        moveCumulativeFileToCurrent(resultFile);

        deleteOldPatchFiles(xmlFiles);
    }

    private static List<String> fetchAndValidatePatchFiles() {
        String[] files = CHANGELOG_PATCHES_DIR.list((_, name) -> name.toLowerCase().endsWith(".xml"));

        if (files == null)
            return List.of();

        LinkedList<String> xmlFiles = new LinkedList<>(Arrays.asList(files));

        if (xmlFiles.isEmpty())
            return List.of();

        xmlFiles.removeIf(fileName -> {
            try {
                var content = Files.readString(new File(CHANGELOG_PATCHES_DIR, fileName).toPath());
                return content.contains("<%s>".formatted(TAG_CHANGELOG_ROOT));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        return xmlFiles;
    }

    private static void fetchByPatches(List<String> xmlFiles) {
        for (String xmlFile : xmlFiles) {
            try {
                var xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = xmlBuilder.parse(new File(CHANGELOG_PATCHES_DIR, xmlFile));

                var rootElement = doc.getDocumentElement();
                if (rootElement.getTagName().equals(TAG_CHANGELOG_ROOT))
                    continue;

                if (!rootElement.getTagName().equals(TAG_PATCH_ROOT))
                    throw new ParseException("No <%s> defined".formatted(TAG_PATCH_ROOT), 0);

                parseBreakingTags(rootElement, additionsList, refactoringsList, fixesList, removalsList);

                parseTags(TAG_ADDITIONS,  rootElement, additionsList,    BreakingKind.NONE);
                parseTags(TAG_REFACTORED, rootElement, refactoringsList, BreakingKind.NONE);
                parseTags(TAG_FIXED,      rootElement, fixesList,        BreakingKind.NONE);
                parseTags(TAG_REMOVALS,   rootElement, removalsList,     BreakingKind.NONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static File cumulatePatches(String versionStr) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            var xmlBuilder = factory.newDocumentBuilder();
            Document doc = xmlBuilder.newDocument();

            var rootElement = doc.createElement(TAG_CHANGELOG_ROOT);
            rootElement.setAttribute(ATTRIBUTE_VERSION, versionStr);

            addTags(doc, rootElement, TAG_ADDITIONS,  additionsList);
            addTags(doc, rootElement, TAG_REFACTORED, refactoringsList);
            addTags(doc, rootElement, TAG_FIXED,      fixesList);
            addTags(doc, rootElement, TAG_REMOVALS,   removalsList);

            doc.appendChild(rootElement);

            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            final String fileName = "v%s_cumulated.xml".formatted(versionStr);

            try (FileOutputStream out = new FileOutputStream(new File(CHANGELOG_PATCHES_DIR, fileName))) {
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }

            return new File(CHANGELOG_PATCHES_DIR, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void deleteOldPatchFiles(List<String> files) {
        for (String xmlFile : files) {
            if (xmlFile.contains("_cumulated"))
                continue;

            try {
                Files.deleteIfExists(new File(CHANGELOG_PATCHES_DIR, xmlFile).toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void moveCumulativeFileToCurrent(File cumulativeFile) {
        if (cumulativeFile == null) {
            System.err.println("No cumulative file to move.");
            return;
        }

        File archiveDir = CHANGELOG_CUMULATED_DIR;
        if (!archiveDir.exists()) {
            if (!archiveDir.mkdirs()) {
                System.err.println("Failed to create archive directory: " + archiveDir);
                return;
            }
        }

        Path source = cumulativeFile.toPath();
        Path target = archiveDir.toPath().resolve(cumulativeFile.getName());

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception _) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
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
        return "";
    }

    private static void addTags(Document doc, Element root, String tag, List<XmlEntry> list) {
        Map<XmlEntryCategory, Map<BreakingKind, List<String>>> sorted =
                list.stream().collect(Collectors.groupingBy(
                        XmlEntry::category,
                        Collectors.groupingBy(
                                XmlEntry::breakingKind,
                                Collectors.mapping(
                                        XmlEntry::content,
                                        Collectors.toList()
                                )
                        )
                ));

        List<XmlEntryCategory> orderedCategories = sorted.keySet()
                .stream()
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .toList();

        for (var category : orderedCategories) {
            Map<BreakingKind, List<String>> breakingMap = sorted.get(category);

            Element categoryElement = doc.createElement(TAG_CATEGORY);
            categoryElement.setAttribute(ATTRIBUTE_CATEGORY, category.label);

            List<BreakingKind> orderedKinds = breakingMap.keySet()
                    .stream()
                    .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                    .toList();

            for (BreakingKind kind : orderedKinds) {

                List<String> entries = breakingMap.get(kind);

                Element container = categoryElement;

                if (kind != BreakingKind.NONE) {
                    Element breakingEl = doc.createElement(TAG_BREAKING);
                    breakingEl.setAttribute(ATTRIBUTE_BREAKING_KIND, kind.value);
                    container.appendChild(breakingEl);
                    container = breakingEl;
                }

                Element tagElement = doc.createElement(tag);

                for (String item : entries) {
                    Element itemElement = doc.createElement(TAG_ENTRY);
                    itemElement.setTextContent(item);
                    tagElement.appendChild(itemElement);
                }

                container.appendChild(tagElement);
            }

            root.appendChild(categoryElement);
        }
    }

    private static void parseTags(String tag, Element scope, List<XmlEntry> listToAdd, BreakingKind kind)
            throws ParseException {

        var categoryNodes = directChildrenByTag(scope, TAG_CATEGORY);

        for (Element categoryEl : categoryNodes) {

            String nodeCat = categoryEl.getAttribute(ATTRIBUTE_CATEGORY);
            XmlEntryCategory category = XmlEntryCategory.fromValue(nodeCat);

            var tagNodes = directChildrenByTag(categoryEl, tag);

            if (tagNodes.size() > 1)
                throw new ParseException("More than one <%s> tag inside category '%s'"
                        .formatted(tag, nodeCat), 0);

            for (Element tagNode : tagNodes) {

                var entries = directChildrenByTag(tagNode, TAG_ENTRY);

                for (Element entry : entries) {
                    String text = entry.getTextContent();
                    if (text != null && !text.isBlank()) {
                        listToAdd.add(new XmlEntry(category, text.trim(), kind));
                    }
                }
            }
        }
    }

    private static void parseBreakingTags(Element rootElement,
                                          List<XmlEntry> additions,
                                          List<XmlEntry> refactorings,
                                          List<XmlEntry> fixes,
                                          List<XmlEntry> removals)
            throws ParseException {

        var categoryNodes = directChildrenByTag(rootElement, TAG_CATEGORY);

        for (Element categoryEl : categoryNodes) {

            String nodeCat = categoryEl.getAttribute(ATTRIBUTE_CATEGORY);
            XmlEntryCategory category = XmlEntryCategory.fromValue(nodeCat);

            var breakingNodes = directChildrenByTag(categoryEl, TAG_BREAKING);

            for (Element breakingEl : breakingNodes) {

                String kind = breakingEl.getAttribute(ATTRIBUTE_BREAKING_KIND);

                if (kind == null || kind.isBlank()) {
                    throw new ParseException(
                            "<%s> missing required attribute '%s'"
                                    .formatted(TAG_BREAKING, ATTRIBUTE_BREAKING_KIND),
                            0
                    );
                }

                BreakingKind breakingKind = BreakingKind.fromValue(kind);

                parseBreakingTag(
                        breakingEl,
                        TAG_ADDITIONS,
                        additions,
                        category,
                        breakingKind
                );

                parseBreakingTag(
                        breakingEl,
                        TAG_REFACTORED,
                        refactorings,
                        category,
                        breakingKind
                );

                parseBreakingTag(
                        breakingEl,
                        TAG_FIXED,
                        fixes,
                        category,
                        breakingKind
                );

                parseBreakingTag(
                        breakingEl,
                        TAG_REMOVALS,
                        removals,
                        category,
                        breakingKind
                );
            }
        }
    }

    private static void parseBreakingTag(Element breakingEl,
                                         String tag,
                                         List<XmlEntry> list,
                                         XmlEntryCategory category,
                                         BreakingKind kind) {

        var wrappers = directChildrenByTag(breakingEl, tag);

        for (Element wrapper : wrappers) {

            var entries = directChildrenByTag(wrapper, TAG_ENTRY);

            for (Element entry : entries) {

                String text = entry.getTextContent();

                if (text == null || text.isBlank())
                    continue;

                list.add(new XmlEntry(category, text.trim(), kind));
            }
        }
    }

    private static void parseChildTagsFromBreaking(Element breakingEl, BreakingKind kind,
                                                   List<XmlEntry> additions, List<XmlEntry> refactorings,
                                                   List<XmlEntry> fixes, List<XmlEntry> removals)
            throws ParseException {
        parseTags(TAG_ADDITIONS,  breakingEl, additions,    kind);
        parseTags(TAG_REFACTORED, breakingEl, refactorings, kind);
        parseTags(TAG_FIXED,      breakingEl, fixes,        kind);
        parseTags(TAG_REMOVALS,   breakingEl, removals,     kind);
    }

    private static void addEntriesToList(NodeList entries, List<XmlEntry> listToAdd, XmlEntryCategory category,
                                         BreakingKind kind) {
        for (int i = 0; i < entries.getLength(); i++) {
            var node = entries.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var entryEl = (Element) node;
            String text = entryEl.getTextContent();

            if (text == null || text.isBlank())
                continue;

            listToAdd.add(new XmlEntry(category, text.trim(), kind));
        }
    }

    private static List<Element> directChildrenByTag(Element parent, String tag) {
        List<Element> result = new ArrayList<>();

        var children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (!node.getNodeName().equals(tag))
                continue;

            result.add((Element) node);
        }

        return result;
    }

    private enum XmlEntryCategory {
        UNKNOWN("unknown", "unknown"),
        API("API", "api"),
        CORE("Core", "core"),
        DEFAULT_INSTRUCTION_SET("Default Instruction Set", "instructions"),
        DEV_TOOLS("Development Tools", "dev-tools");

        public final String label;
        public final String attr;

        XmlEntryCategory(String name, String attr) {
            this.label = name;
            this.attr = attr;
        }

        static XmlEntryCategory fromValue(String catStr) {
            for (var xmlCat : XmlEntryCategory.values()) {
                if (xmlCat.attr.equals(catStr))
                    return xmlCat;
            }
            return UNKNOWN;
        }
    }

    private enum BreakingKind {
        NONE(null),
        TRIVIAL("trivial"),
        MINOR("minor"),
        MAJOR("major");

        public final String value;

        BreakingKind(String value) {
            this.value = value;
        }

        static BreakingKind fromValue(String breStr) {
            for (var breakingKind : BreakingKind.values()) {
                if (breakingKind == NONE)
                    continue;
                if (breakingKind.value.equals(breStr))
                    return breakingKind;
            }
            return NONE;
        }
    }

    private record XmlEntry(XmlEntryCategory category, String content, BreakingKind breakingKind) {}
}
