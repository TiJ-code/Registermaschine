package dk.tij.registermaschine.core.config.internal;

public final class XmlConstants {
    private XmlConstants() {}

    public static final String  TAG_INSTRUCTION = "instruction",
                                TAG_OPERAND = "operand";

    public static final String  ATTRIBUTE_CONDITION_MACRO_NAME = "name",
                                ATTRIBUTE_CONDITION_MACRO_VALUE = "value";

    public static final String  ATTRIBUTE_INSTRUCTION_ID = "id",
                                ATTRIBUTE_INSTRUCTION_DESCRIPTION = "description",
                                ATTRIBUTE_INSTRUCTION_HANDLER = "handler",
                                ATTRIBUTE_INSTRUCTION_CONDITION = "condition";

    public static final String  ATTRIBUTE_OPERAND_TYPE = "type",
                                ATTRIBUTE_OPERAND_CONCEPT = "concept",
                                ATTRIBUTE_OPERAND_VALUE = "implicitValue";
}
