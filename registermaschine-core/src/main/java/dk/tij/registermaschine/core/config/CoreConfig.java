package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.api.compilation.lexing.TokenType;
import dk.tij.registermaschine.api.config.ConfigInstruction;

import java.util.*;

public final class CoreConfig {
    private CoreConfig() {}

    public static final String  CORE_IMPLEMENTATION_PREFIX = "core.",
                                CORE_CLASS_PATH_PREFIX = "dk.tij.registermaschine.";

    public static volatile int REGISTERS = 8;
    public static volatile int MAX_JUMPS = 255;
    public static volatile boolean ALLOW_LABELS = true;

    public static final Map<TokenType, String> TOKEN_REGEX = Map.of(
            TokenType.INSTRUCTION, "(?:[A-Z]+|[a-z]+)",
            TokenType.REGISTER, "r[0-9]+",
            TokenType.NUMBER, "#[0-9]+",
            TokenType.COMMENT, ";.*"
    );

    public static final Map<String, String> CONDITION_MACROS = new HashMap<>();

    public static final List<ConfigInstruction> INSTRUCTIONS = new LinkedList<>();
}
