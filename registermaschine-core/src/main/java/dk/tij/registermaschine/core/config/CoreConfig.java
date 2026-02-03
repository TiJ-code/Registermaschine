package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.api.lexing.TokenType;

import java.util.*;

public final class CoreConfig {
    private CoreConfig() {}

    public static int REGISTERS = 8;

    public static final Map<TokenType, String> TOKEN_REGEX = Map.of(
            TokenType.INSTRUCTION, "(?:[A-Z]+|[a-z]+)",
            TokenType.REGISTER, "r[0-9]+",
            TokenType.NUMBER, "#[0-9]+",
            TokenType.COMMENT, ";.*"
    );
}
