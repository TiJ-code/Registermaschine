package dk.tij.registermaschine.core.compilation.lexing;

public record Token(Type type, String value, int line, int column) {
    public enum Type {
        INSTRUCTION,
        REGISTER,
        NUMBER,
        COMMENT,
        EOL,
        EOF,
        UNKNOWN
    }

    @Override
    public String toString() {
        return String.format("%s ('%s')", type, value);
    }
}
