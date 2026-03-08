package dk.tij.registermaschine.core.config.internal;

public final class XmlConstants {
    private XmlConstants() {}

    public static final int VERSION_INSTRUCTION_SET = 2;

    public static final String  TAG_CONFIG_REGISTERS = "registers",
                                TAG_CONFIG_MAX_JUMPS = "maxJumps";

    public static final String  TAG_INSTRUCTION = "instruction",
                                TAG_OPERAND = "operand",
                                TAG_OPTION = "option",
                                TAG_CHAIN = "chain",
                                TAG_STEP = "step",
                                TAG_IN = "in",
                                TAG_OUT = "out";

    public static final String  ATTRIBUTE_INSTRUCTION_SET_VERSION = "version";

    public static final String  ATTRIBUTE_CONDITION_MACRO_NAME = "name",
                                ATTRIBUTE_CONDITION_MACRO_VALUE = "value";

    public static final String  ATTRIBUTE_INSTRUCTION_ID = "id",
                                ATTRIBUTE_INSTRUCTION_DESCRIPTION = "description",
                                ATTRIBUTE_INSTRUCTION_HANDLER = "handler",
                                ATTRIBUTE_INSTRUCTION_CONDITION = "condition";

    public static final String  ATTRIBUTE_OPERAND_TYPE = "type",
                                ATTRIBUTE_OPERAND_CONCEPT = "concept",
                                ATTRIBUTE_OPERAND_NAME = "name",
                                ATTRIBUTE_OPERAND_IMPLICIT_VALUE = "implicitValue";

    public static final String  ATTRIBUTE_STEP_HANDLER = "handler",
                                ATTRIBUTE_STEP_CONDITION = "condition";

    public static final String  ATTRIBUTE_IN_REF = "ref",
                                ATTRIBUTE_OUT_TO = "to";

    public static final String  ATTRIBUTE_OPTION_ID = "id",
                                ATTRIBUTE_OPTION_VALUE = "value";

    public static final String  VALUE_CONCEPT_RESULT = "result",
                                VALUE_CONCEPT_TARGET = "target",
                                VALUE_CONCEPT_OPERAND = "operand";

    public static final String INSTR_OPTION_ALLOW_LABELS = "allowLabels";
}
