package dk.tij.registermaschine.core.config.internal.migration;

import dk.tij.registermaschine.core.config.internal.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Handles migration of instruction set XML files to the latest version.
 *
 * <p>This class ensures that older instruction set files are updated to
 * conform to the current XML schema expected by the system. If migration
 * is required, it creates a backup of the original file and writes the
 * updated version to disk.</p>
 *
 * <p>Current supported migration:</p>
 * <ul>
 *     <li>Version 1 -> Version 2: converts legacy instruction handler attributes
 *     to chained step elements with proper input/output mapping.</li>
 * </ul>
 *
 * <p>Usage is static; this class cannot be instantiated</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class InstructionSetMigrator {
    /**
     * Private constructor to prevent instantiation.
     */
    private InstructionSetMigrator() {}

    /**
     * Migrates the given instruction set document if needed and stores it back
     * to the specified path. Creates a backup of the original file and deletes
     * it if no migration occurred.
     *
     * @param doc the DOM document representing the instruction set
     * @param path the file path of the instruction set XML
     */
    public static void migrateAndStoreIfNeeded(Document doc, Path path) {
        backupOldFile(path);
        boolean migrationHappened = migrateIfNeeded(doc);

        if (migrationHappened) {
            writeDocument(doc, path);
        } else {
            deleteBackupFile(path);
        }
    }

    /**
     * Determines whether migration is needed for the document, and performs
     * migration if required.
     *
     * @param doc the DOM document
     * @return {@code true} if migration was performed; {@code false} if the document already up-to-date
     */
    private static boolean migrateIfNeeded(Document doc) {
        Element root = doc.getDocumentElement();

        if (root.hasAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_SET_VERSION)) {
            int instrSetVersion = Integer.parseInt(root.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_SET_VERSION));
            if (instrSetVersion >= XmlConstants.VERSION_INSTRUCTION_SET)
                return false;
        }

        migrateV1toV2(doc, root);

        return true;
    }

    /**
     * Creates backup of the given file with the {@code .old} extension.
     *
     * @param path the path of the file to back up
     */
    private static void backupOldFile(Path path) {
        try {
            Files.copy(path, Path.of(path + ".old"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the backup file corresponding to the given path.
     *
     * @param path the original file path whose backup should be deleted
     */
    private static void deleteBackupFile(Path path) {
        File backupFile = new File(Path.of(path + ".old").toUri());
        if (backupFile.exists()) {
            backupFile.delete();
        }
    }

    /**
     * Writes the DOM document to the specified file path using pretty-printing
     *
     * @param doc the DOM document to write
     * @param path the file path to write to
     */
    private static void writeDocument(Document doc, Path path) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            try (OutputStream os = Files.newOutputStream(path)) {
                transformer.transform(
                        new DOMSource(doc),
                        new StreamResult(os)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write migrated instruction set", e);
        }
    }

    /**
     * Migrates an instruction set from version 1 to version 2.
     *
     * <p>This migration replaces legacy instruction handler attributes with
     * chained step elements, generates operand names based on their concept,
     * and maps core instructions to their corresponding step handlers.</p>
     *
     * @param doc the DOM document
     * @param root the root element of the instruction set
     */
    private static void migrateV1toV2(Document doc, Element root) {
        final Map<String, String> coreInstructionHandlerMapping = Map.of(
                "dk.tij.registermaschine.instructions.AdditionInstruction", "dk.tij.registermaschine.instructions.AdditionStepHandler",
                "dk.tij.registermaschine.instructions.SubtractionInstruction", "dk.tij.registermaschine.instructions.SubtractionStepHandler",
                "dk.tij.registermaschine.instructions.MultiplicationInstruction", "dk.tij.registermaschine.instructions.MultiplicationStepHandler",
                "dk.tij.registermaschine.instructions.DivisionInstruction", "dk.tij.registermaschine.instructions.DivisionStepHandler",
                "dk.tij.registermaschine.instructions.HaltInstruction", "dk.tij.registermaschine.instructions.HaltStepHandler",
                "dk.tij.registermaschine.instructions.InputInstruction", "dk.tij.registermaschine.instructions.InputStepHandler",
                "dk.tij.registermaschine.instructions.OutputInstruction", "dk.tij.registermaschine.instructions.OutputStepHandler",
                "dk.tij.registermaschine.instructions.JumpInstruction", "dk.tij.registermaschine.instructions.JumpStepHandler",
                "dk.tij.registermaschine.instructions.MoveInstruction", "dk.tij.registermaschine.instructions.MoveStepHandler"
        );

        NodeList instructions = root.getElementsByTagName(XmlConstants.TAG_INSTRUCTION);

        for (int i = 0; i < instructions.getLength(); i++) {
            Element instruction = (Element) instructions.item(i);

            String handler = instruction.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_HANDLER);

            if (coreInstructionHandlerMapping.containsKey(handler)) {
                handler = coreInstructionHandlerMapping.get(handler);
            }

            String condition = instruction.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_CONDITION);

            instruction.removeAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_HANDLER);

            Element chain = doc.createElement(XmlConstants.TAG_CHAIN);
            Element step = doc.createElement(XmlConstants.TAG_STEP);

            if (!handler.isEmpty())
                step.setAttribute(XmlConstants.ATTRIBUTE_STEP_HANDLER, handler);
            if (!condition.isEmpty())
                step.setAttribute(XmlConstants.ATTRIBUTE_STEP_CONDITION, condition);

            NodeList operands = instruction.getElementsByTagName(XmlConstants.TAG_OPERAND);

            int operandCounter = 1;
            String resultName = null;

            for (int j = 0; j < operands.getLength(); j++) {
                Node operandNode = operands.item(j);

                if (!(operandNode instanceof Element operand))
                    continue;

                if (!operand.getTagName().equals(XmlConstants.TAG_OPERAND))
                    continue;

                String concept = operand.getAttribute(XmlConstants.ATTRIBUTE_OPERAND_CONCEPT);

                String name;
                switch (concept) {
                    case XmlConstants.VALUE_CONCEPT_RESULT -> {
                        name = XmlConstants.VALUE_CONCEPT_RESULT;
                        resultName = name;
                    }
                    case XmlConstants.VALUE_CONCEPT_TARGET -> name = XmlConstants.VALUE_CONCEPT_TARGET;
                    case XmlConstants.VALUE_CONCEPT_OPERAND -> name = XmlConstants.VALUE_CONCEPT_OPERAND + operandCounter++;
                    default -> name = "arg" + operandCounter++;
                }

                operand.setAttribute(XmlConstants.ATTRIBUTE_OPERAND_NAME, name);
                System.out.println("generated name: " + name);

                if (concept.equals(XmlConstants.VALUE_CONCEPT_OPERAND) || concept.equals(XmlConstants.VALUE_CONCEPT_TARGET)) {
                    Element in = doc.createElement(XmlConstants.TAG_IN);
                    in.setAttribute(XmlConstants.ATTRIBUTE_IN_REF, name);
                    step.appendChild(in);
                }
            }

            Element out = doc.createElement(XmlConstants.TAG_OUT);

            if (resultName != null) {
                out.setAttribute(XmlConstants.ATTRIBUTE_OUT_TO, resultName);
            }

            step.appendChild(out);

            chain.appendChild(step);
            instruction.appendChild(chain);
        }

        root.setAttribute(
                XmlConstants.ATTRIBUTE_INSTRUCTION_SET_VERSION,
                "2"
        );
    }
}
