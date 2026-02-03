package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.compilation.api.lexing.TokenType;

import java.util.*;

public final class Config {
    private Config() {}

    public static int REGISTERS = 8;

    public static final Set<String> INSTRUCTIONS = new HashSet<>();

    public static final Map<TokenType, String> TOKEN_REGEX = Map.of(
            TokenType.INSTRUCTION, "(?:[A-Z]+|[a-z]+)",
            TokenType.REGISTER, "r[0-9]+",
            TokenType.NUMBER, "#[0-9]+",
            TokenType.COMMENT, ";.*"
    );

    public static Map<TokenType, String> TOKEN_COLOUR = Map.of(
            TokenType.INSTRUCTION, "#123456",
            TokenType.REGISTER, "#123456",
            TokenType.NUMBER, "#123456",
            TokenType.COMMENT, "#123789"
    );
}
