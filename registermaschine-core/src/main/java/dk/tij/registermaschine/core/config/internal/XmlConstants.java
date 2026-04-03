package dk.tij.registermaschine.core.config.internal;

/**
 * A collection of constant identifiers used for parsing XML configuration files
 *
 * <p>This utility class centralises all tag and attribute names to prevent
 * "magic strings" throughout the parser implementations and to ensure
 * consistent XML formatting.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class XmlConstants {
    /**
     * Private constructor to prevent instantiation of constant holder
     */
    private XmlConstants() {}

    public static final int VERSION_INSTRUCTION_SET = 2;

    /**
     * Tag for the number of registers (e.g., {@code <registers>8</registers>}
     */
    public static final String  TAG_CONFIG_REGISTERS = "registers",
    /**
     * Tag for the max number of jumps (e.g., {@code <maxJumps>8</maxJumps>}
     */
                                TAG_CONFIG_MAX_JUMPS = "maxJumps",
    /**
     * Tag for custom config keys (e.g., {@code <custom key="custom.entry">value</custom>})
     */
                                TAG_CONFIG_CUSTOM = "custom";

    /**
     * Tag defining a machine instruction
     */
    public static final String  TAG_INSTRUCTION = "instruction",
    /**
     * Tag defining an operand within an instruction
     */
                                TAG_OPERAND = "operand",
    /**
     * Tag for global configuration toggles
     */
                                TAG_OPTION = "option",
                                TAG_CHAIN = "chain",
                                TAG_STEP = "step",
                                TAG_IN = "in",
                                TAG_OUT = "out";

    public static final String  ATTRIBUTE_INSTRUCTION_SET_VERSION = "version";

    /**
     * Attribute for the name of a condition macro
     */
    public static final String  ATTRIBUTE_CONDITION_MACRO_NAME = "name",
    /**
     * Attribute for the logical expression assigned to a macro
     */
                                ATTRIBUTE_CONDITION_MACRO_VALUE = "value";

    /**
     * The mnemonic ID
     */
    public static final String  ATTRIBUTE_INSTRUCTION_ID = "id",
    /**
     * A human-readable summary of the instruction
     */
                                ATTRIBUTE_INSTRUCTION_DESCRIPTION = "description",
    /**
     * The fully qualified Java class name of the handler
     */
                                ATTRIBUTE_INSTRUCTION_HANDLER = "handler",
    /**
     * The logical activation condition string
     */
                                ATTRIBUTE_INSTRUCTION_CONDITION = "condition";

    public static final String  ATTRIBUTE_OPERAND_NAME = "name",
    /**
     * The data type of the operand
     */
                                ATTRIBUTE_OPERAND_TYPE = "type",
    /**
     * The semantic role of the operand
     */
                                ATTRIBUTE_OPERAND_CONCEPT = "concept",
    /**
     * An optional hardcoded value for the operand
     */
                                ATTRIBUTE_OPERAND_IMPLICIT_VALUE = "implicitValue";

    public static final String  ATTRIBUTE_STEP_HANDLER = "handler",
                                ATTRIBUTE_STEP_CONDITION = "condition";

    public static final String  ATTRIBUTE_IN_REF = "ref";

    public static final String  ATTRIBUTE_OUT_TO = "to";

    /**
     * The identifier for a specific configuration option
     */
    public static final String  ATTRIBUTE_OPTION_ID = "id",
    /**
     * The value assigned to a configuration option
     */
                                ATTRIBUTE_OPTION_VALUE = "value";

    public static final String  VALUE_CONCEPT_RESULT = "result",
                                VALUE_CONCEPT_TARGET = "target",
                                VALUE_CONCEPT_OPERAND = "operand";

    /**
     * Option ID toggle for enabling / disabling label support in assembly
     */

    public static final String INSTR_OPTION_ALLOW_LABELS = "allowLabels";
}
