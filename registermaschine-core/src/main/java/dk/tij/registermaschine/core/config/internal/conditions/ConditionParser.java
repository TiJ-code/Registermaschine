package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.api.log.ILogger;
import dk.tij.registermaschine.api.log.LoggerFactory;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.AndNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.ConditionToken;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.LeafNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.MacroNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.NotNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.OrNode;

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
    private static final ILogger LOGGER = LoggerFactory.getLogger(ConditionParser.class);

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
        LOGGER.debug("Starting parsing of {} tokens", conditionTokens.size());

        ParseContext context = new ParseContext(conditionTokens);

        ConditionNode expression = parseOr(context);
        expect(context, ConditionToken.Type.EOF);

        LOGGER.debug("Finished parsing. Final AST: {}", expression);
        return expression;
    }

    /**
     * Parses logical OR expressions (left-associative)
     *
     * @param context the current parse context
     * @return the parsed {@link OrNode}
     */
    private static ConditionNode parseOr(ParseContext context) {
        LOGGER.trace("Entering parseOr at position {}", context.pos);

        ConditionNode node = parseAnd(context);
        while (match(context, ConditionToken.Type.OR)) {
            LOGGER.trace("Matched OR at position {}", context.pos - 1);
            node = new OrNode(node, parseAnd(context));
            LOGGER.debug("Built OR node: {}", node);
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
        LOGGER.trace("Entering parseAnd at position {}", context.pos);

        ConditionNode node = parseUnary(context);
        while (match(context, ConditionToken.Type.AND)) {
            LOGGER.trace("Matched AND at position {}", context.pos - 1);
            node = new AndNode(node, parseUnary(context));
            LOGGER.debug("Built AND node: {}", node);
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
        LOGGER.trace("Entering parseUnary at position {}", context.pos);

        if (match(context, ConditionToken.Type.NOT)) {
            LOGGER.trace("Matched NOT at position {}", context.pos - 1);
            var node = new NotNode(parseUnary(context));
            LOGGER.debug("Built NOT node: {}", node);
            return node;
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
        LOGGER.trace("Entering parsePrimary at position {}", context.pos);

        if (match(context, ConditionToken.Type.MACRO)) {
            var name = previous(context).text();
            LOGGER.trace("Matched {} '{}'", ConditionToken.Type.MACRO, name);
            var node = new MacroNode(name);
            LOGGER.debug("Built {} not: {}", ConditionToken.Type.MACRO, node);
            return node;
        }

        if (match(context, ConditionToken.Type.IDENTIFIER)) {
            var name = previous(context).text();
            LOGGER.trace("Matched {} '{}'", ConditionToken.Type.IDENTIFIER, name);
            var node = new LeafNode(name);
            LOGGER.debug("Built {} not: {}", ConditionToken.Type.MACRO, node);
            return node;
        }

        if (match(context, ConditionToken.Type.LEFT_PAREN)) {
            LOGGER.trace("Matched {} at position {}", ConditionToken.Type.LEFT_PAREN, context.pos - 1);
            ConditionNode expr = parseOr(context);
            expect(context, ConditionToken.Type.RIGHT_PAREN);
            LOGGER.trace("Closed parenthesis");
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
        var token = peek(context);

        if (token.type() == type) {
            LOGGER.trace("Matched token {} at position {}", token, context.pos);
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
            LOGGER.error("Expected token {} but found {}", type, peek(context));
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
        var token = peek(context);
        LOGGER.error("Parse error: {} at token {} (position {})", msg, token, context.pos);

        return new RuntimeException(
                msg + " at token " + token
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
