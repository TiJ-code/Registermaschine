package dk.tij.registermaschine.core.config;

import dk.tij.registermaschine.core.parser.Token;

import java.util.Map;

public final class ConfigRegistry {
    private ConfigRegistry() {}

    public static final Map<Token.Type, String> TOKEN_REGEX = Map.of(
            Token.Type.REGISTER, "r[0-7]",
            Token.Type.NUMBER, "[0-9]*",
            Token.Type.COMMENT, ";[A-Za-z0-9]*"
    );
}
