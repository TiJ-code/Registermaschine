package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.parser.Token;

import java.util.Map;

public final class Config {
    private Config() {}

    public static int REGISTERS = 8;

    public static final Map<Token.Type, String> TOKEN_REGEX = Map.of(
            Token.Type.INSTRUCTION, "(?:[A-Z]+|[a-z]+)",
            Token.Type.REGISTER, "r[0-7]",
            Token.Type.NUMBER, "[0-9]*",
            Token.Type.COMMENT, ";.*"
    );

    public static Map<Token.Type, String> TOKEN_COLOUR = Map.of(
            Token.Type.INSTRUCTION, "#123456",
            Token.Type.REGISTER, "#123456",
            Token.Type.NUMBER, "#123456",
            Token.Type.COMMENT, "#123789"
    );
}
