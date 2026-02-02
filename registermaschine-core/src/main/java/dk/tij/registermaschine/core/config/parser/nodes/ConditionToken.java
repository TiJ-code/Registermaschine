package dk.tij.registermaschine.core.config.parser.nodes;

public record ConditionToken(Type type, String text) {
    public enum Type {
        IDENTIFIER,
        NOT,
        AND,
        OR,
        LEFT_PAREN,
        RIGHT_PAREN,
        EOF
    }
}
