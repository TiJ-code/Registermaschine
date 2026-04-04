package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.compilation.lexing.TokenType;
import dk.tij.registermaschine.api.compilation.pre.IPrecompilerStage;
import dk.tij.registermaschine.api.config.model.ConfigInstruction;
import dk.tij.registermaschine.api.instructions.ChainedInstruction;
import dk.tij.registermaschine.api.instructions.IInstructionRegistry;
import dk.tij.registermaschine.api.log.LogConfig;
import dk.tij.registermaschine.core.compilation.internal.pre.InstructionPrecompilerStage;
import dk.tij.registermaschine.core.instructions.ConcreteInstructionRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Core configuration constants and global settings for the Registermaschine.
 *
 * <p>This class contains default values for registers, jump limits, label,
 * instruction lists, and token parsing regex patterns.</p>
 *
 * <p>All fields are static; this class cannot be instantiated.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class CoreConfig {
    private CoreConfig() {}

    public static final LogConfig LOG = LogConfig.instance();

    /**
     * Prefix for core implementations in the system
     */
    public static final String  CORE_IMPLEMENTATION_PREFIX = "core.",
                                CORE_CLASS_PATH_PREFIX = "dk.tij.registermaschine.";

    /**
     * Default number of general-purpose registers
     */
    public static volatile int REGISTERS = 8;
    /**
     * Maximum allowed jumps in a single execution before halting.
     */
    public static volatile int MAX_JUMPS = 255;
    /**
     * Whether label references are allowed in instructions.
     */
    public static volatile boolean ALLOW_LABELS = true;

    /**
     * Regular expressions for token parsing based on token type
     */
    public static final Map<TokenType, String> TOKEN_REGEX = Map.of(
            TokenType.INSTRUCTION, "(?:[A-Z]+|[a-z]+)",
            TokenType.REGISTER, "r[0-9]+",
            TokenType.NUMBER, "#[0-9]+",
            TokenType.COMMENT, ";.*"
    );

    /**
     * Macro definitions for conditions, keyed by macro name
     */
    public static final Map<String, String> CONDITION_MACROS = new HashMap<>();

    /**
     * List of all configured instructions in the runtime
     */
    public static final List<ConfigInstruction> INSTRUCTIONS = new LinkedList<>();

    /**
     * Precompiler stage responsible for converting {@link ConfigInstruction}
     * instances into {@link ChainedInstruction} objects.
     *
     * <p>This stage is used as part of a precompilation pipeline, to directly
     * generate the executable representation of an instruction from its configuration.</p>
     */
    public static final IPrecompilerStage<ConfigInstruction, ChainedInstruction> INSTRUCTION_PRECOMPILER = InstructionPrecompilerStage.instance();

    public static volatile IInstructionRegistry INSTRUCTION_REGISTRY = ConcreteInstructionRegistry.instance();
}
