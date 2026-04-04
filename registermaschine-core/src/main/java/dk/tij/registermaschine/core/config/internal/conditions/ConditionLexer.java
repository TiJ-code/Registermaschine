package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.core.config.internal.conditions.nodes.ConditionToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class responsible for performing lexical analysis on condition strings
 *
 * <p>The lexer scans the input string character by character and groups them into
 * {@link ConditionToken}s based on defined syntax rules (e.g., operators, identifiers, macros)</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConditionLexer {
    /**
     * Private constructor to prevent instantiation of this utility class
     */
    private ConditionLexer() {}

    /**
     * Converts a raw condition string into an ordered list of tokens.
     *
     * <p>Supported syntax:</p>
     * <ul>
     *     <li>{@code !} : NOT operator</li>
     *     <li>{@code *} : AND operator</li>
     *     <li>{@code +} : OR operator</li>
     *     <li>{@code ( )} : Grouping parenthesis</li>
     *     <li>{@code @name} : Macro identifier</li>
     *     <li>{@code path.Name}: Class or identifier</li>
     * </ul>
     *
     * @param source The raw condition string to tokenize.
     * @return A list of {@link ConditionToken} objects ending with an {@link ConditionToken.Type#EOF} token.
     * @throws RuntimeException if an invalid or unexpected character is encountered
     */
    public static List<ConditionToken> tokenize(String source) {
        List<ConditionToken> tokens = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);

        while (i.get() < source.length()) {
            char c = source.charAt(i.get());

            switch (c) {
                case ' ' -> i.incrementAndGet();
                case '!' -> {
                    tokens.add(new ConditionToken(ConditionToken.Type.NOT, "!"));
                    i.incrementAndGet();
                }
                case '*' -> {
                    tokens.add(new ConditionToken(ConditionToken.Type.AND, "*"));
                    i.incrementAndGet();
                }
                case '+' -> {
                    tokens.add(new ConditionToken(ConditionToken.Type.OR, "+"));
                    i.incrementAndGet();
                }
                case '(' -> {
                    tokens.add(new ConditionToken(ConditionToken.Type.LEFT_PAREN, "("));
                    i.incrementAndGet();
                }
                case ')' -> {
                    tokens.add(new ConditionToken(ConditionToken.Type.RIGHT_PAREN, ")"));
                    i.incrementAndGet();
                }

                case '@' -> {
                    i.incrementAndGet();
                    tokens.add(readMacroIdentifier(source, i));
                }

                default -> {
                    if (Character.isJavaIdentifierStart(c)) {
                        tokens.add(readIdentifier(source, i));
                    } else {
                        throw new RuntimeException("Unexpected char: " + c);
                    }
                }
            }
        }

        tokens.add(new ConditionToken(ConditionToken.Type.EOF, null));
        return tokens;
    }

    /**
     * Consumes characters to form a standard identifier (e.g., a class name).
     *
     * @param source The raw source string
     * @param currentIndex The current character index
     * @return the parsed {@link ConditionToken}
     */
    private static ConditionToken readIdentifier(String source, AtomicInteger currentIndex) {
        int start = currentIndex.get();

        while (currentIndex.get() < source.length() && isValidIdentifierChar(source.charAt(currentIndex.get())))
            currentIndex.incrementAndGet();

        return new ConditionToken(ConditionToken.Type.IDENTIFIER, source.substring(start, currentIndex.get()));
    }

    /**
     * Consumes characters to form a macro identified following an '@' symbol.
     *
     * @param source The raw source string
     * @param currentIndex The current character index
     * @return the parsed {@link ConditionToken}
     */
    private static ConditionToken readMacroIdentifier(String source, AtomicInteger currentIndex) {
        int start = currentIndex.get();

        while (currentIndex.get() < source.length() && isValidIdentifierChar(source.charAt(currentIndex.get())))
            currentIndex.incrementAndGet();

        String name = source.substring(start, currentIndex.get());
        return new ConditionToken(ConditionToken.Type.MACRO, name);
    }

    /**
     * Determines if  a character is valid for an identifier.
     * Includes '.' to support fully qualified class names.
     *
     * @param c The character to check
     * @return {@code true} if the character is valid
     */
    private static boolean isValidIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || c == '.';
    }
}
