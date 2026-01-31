package dk.tij.registermaschine.core.parser;

public class Token {
    public enum Type {
        INSTRUCTION,
        REGISTER,
        NUMBER,
        COMMENT,
        EOL,
        EOF,
        UNKNOWN
    }

    Type type;
    String value;
    int line, column;

    public Token(final Type type, final String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("%s ('%s')", type, value);
    }
}
