package dk.tij.registermaschine.core.config.internal.conditions.nodes;

/**
 * Represents a basic unit of syntax discovered during the lexing of a condition string.
 *
 * @param type The classification of the token (e.g., a logical operator or a name).
 * @param text The literal string content of the token.
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConditionToken(Type type, String text) {
    /**
     * Defines the valid lexical types for the condition language.
     */
    public enum Type {
        /**
         * A raw name, usually representing a class or identifier
         */
        IDENTIFIER,

        /**
         * The logical 'NOT' operator
         */
        NOT,

        /**
         * The logical 'AND' operator
         */
        AND,

        /**
         * The logical 'OR' operator
         */
        OR,

        /**
         * An opening parenthesis for grouping
         */
        LEFT_PAREN,

        /**
         * A closing parenthesis for grouping
         */
        RIGHT_PAREN,

        /**
         * A reference to a predefined macro
         */
        MACRO,

        /**
         * The end-of-file marker
         */
        EOF
    }
}
