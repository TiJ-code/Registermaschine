package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.core.config.internal.conditions.nodes.*;

import java.util.List;

/**
 * A recursive descent parser that converts a list of {@link ConditionToken}s
 * into a tree of {@link ConditionNode}s.
 *
 * <p>The parser enforces operator precedence and handles logical grouping.
 * The grammar follows this hierarchy (lowest to highest precedence):</p>
 * <ol>
 *     <li>OR (+)</li>
 *     <li>AND (*)</li>
 *     <li>Unary NOT (!)</li>
 *     <li>Primary (Identifiers, Macros, Parentheses)</li>
 * </ol>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConditionParser {
    /**
     * Private constructor to prevent instantiation of this utility class
     */
    private ConditionParser() {}

    /**
     * Entry point for parsing a token stream.
     *
     * @param conditionTokens The list of tokens to process
     * @return The root {@link ConditionNode} of resulting syntax tree.
     * @throws RuntimeException if the syntax is invalid or the {@link ConditionToken.Type#EOF} is missing.
     */
    public static ConditionNode parse(List<ConditionToken> conditionTokens) {
        ParseContext context = new ParseContext(conditionTokens);

        ConditionNode expression = parseOr(context);
        expect(context, ConditionToken.Type.EOF);
        return expression;
    }

    /**
     * Parses logical OR expressions (left-associative)
     *
     * @param context the current parse context
     * @return the parsed {@link OrNode}
     */
    private static ConditionNode parseOr(ParseContext context) {
        ConditionNode node = parseAnd(context);
        while (match(context, ConditionToken.Type.OR)) {
            node = new OrNode(node, parseAnd(context));
        }
        return node;
    }

    /**
     * Parses logical AND expressions (left-associative)
     *
     * @param context the current parse context
     * @return the parsed {@link AndNode}
     */
    private static ConditionNode parseAnd(ParseContext context) {
        ConditionNode node = parseUnary(context);
        while (match(context, ConditionToken.Type.AND)) {
            node = new AndNode(node, parseUnary(context));
        }
        return node;
    }

    /**
     * Parses unary operators (e.g., logical NOT)
     *
     * @param context the current parse context
     * @return the parsed {@link NotNode}
     */
    private static ConditionNode parseUnary(ParseContext context) {
        if (match(context, ConditionToken.Type.NOT)) {
            return new NotNode(parseUnary(context));
        }
        return parsePrimary(context);
    }

    /**
     * Parses terminal values or grouped expressions within parentheses
     *
     * @param context the current parse context
     * @return the parsed {@link ConditionNode}
     */
    private static ConditionNode parsePrimary(ParseContext context) {
        if (match(context, ConditionToken.Type.MACRO)) {
            return new MacroNode(previous(context).text());
        }

        if (match(context, ConditionToken.Type.IDENTIFIER)) {
            return new LeafNode(previous(context).text());
        }

        if (match(context, ConditionToken.Type.LEFT_PAREN)) {
            ConditionNode expr = parseOr(context);
            expect(context, ConditionToken.Type.RIGHT_PAREN);
            return expr;
        }

        throw error(context, "Expected condition or '('");
    }

    /**
     * Checks if the current token matches the type; consumes if it does
     *
     * @param context the current parse context
     * @param type the token type to be matched against
     * @return {@code true} if the next token is of {@code type}
     */
    private static boolean match(ParseContext context, ConditionToken.Type type) {
        if (peek(context).type() == type) {
            context.pos++;
            return true;
        }
        return false;
    }

    /**
     * Consumes the current token if it matches, otherwise throws an error.
     *
     * @param context the current parse context
     * @param type the token type to be matched against
     * @throws RuntimeException if token did not match
     */
    private static void expect(ParseContext context, ConditionToken.Type type) {
        if (!match(context, type)) {
            throw error(context, "Expected " + type);
        }
    }

    /**
     * Returns the current token without consuming it.
     *
     * @param context the current parse context
     * @return the current token
     */
    private static ConditionToken peek(ParseContext context) {
        return context.tokens.get(context.pos);
    }

    /**
     * Returns the previous token without consuming it.
     *
     * @param context the current parse context
     * @return the previous token
     */
    private static ConditionToken previous(ParseContext context) {
        return context.tokens.get(context.pos - 1);
    }

    /**
     * Creates a detailed error message with token context
     * @param context the current parse context
     * @param msg the message to be displayed
     * @return a {@link RuntimeException} instance
     */
    private static RuntimeException error(ParseContext context, String msg) {
        return new RuntimeException(
                msg + " at token " + peek(context)
        );
    }

    /**
     * Internal state container to track the parser's position in the token stream
     */
    static final class ParseContext {
        final List<ConditionToken> tokens;
        int pos = 0;

        ParseContext(List<ConditionToken> tokens) {
            this.tokens = tokens;
        }
    }
}
