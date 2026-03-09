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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InstructionSetMigrator {
    private InstructionSetMigrator() {}

    public static void migrateAndStoreIfNeeded(Document doc, Path path) {
        boolean migrationHappened = migrateIfNeeded(doc);

        if (migrationHappened) {
            writeDocument(doc, path);
        }
    }

    private static boolean migrateIfNeeded(Document doc) {
        Element root = doc.getDocumentElement();

        if (root.hasAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_SET_VERSION)) {
            int instrSetVer = Integer.parseInt(root.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_SET_VERSION));
            if (instrSetVer >= XmlConstants.VERSION_INSTRUCTION_SET)
                return false;
        }

        migrateV1toV2(doc, root);
        return true;
    }

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

    private static void migrateV1toV2(Document doc, Element root) {
        NodeList instructions = root.getElementsByTagName(XmlConstants.TAG_INSTRUCTION);

        for (int i = 0; i < instructions.getLength(); i++) {
            Element instruction = (Element) instructions.item(i);

            String handler = instruction.getAttribute(XmlConstants.ATTRIBUTE_INSTRUCTION_HANDLER);
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
                    case XmlConstants.VALUE_CONCEPT_OPERAND -> name = XmlConstants.VALUE_CONCEPT_OPERAND + String.valueOf(operandCounter++);
                    default -> name = "arg" + operandCounter++;
                }

                operand.setAttribute(XmlConstants.ATTRIBUTE_OPERAND_NAME, name);

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
                String.valueOf(XmlConstants.VERSION_INSTRUCTION_SET)
        );
    }
}
