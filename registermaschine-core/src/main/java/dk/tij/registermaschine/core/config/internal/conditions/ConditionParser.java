package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.core.config.internal.conditions.nodes.*;

import java.util.List;

public final class ConditionParser {
    private ConditionParser() {}

    public static ConditionNode parse(List<ConditionToken> conditionTokens) {
        ParseContext context = new ParseContext(conditionTokens);

        ConditionNode expression = parseOr(context);
        expect(context, ConditionToken.Type.EOF);
        return expression;
    }

    private static ConditionNode parseOr(ParseContext context) {
        ConditionNode node = parseAnd(context);
        while (match(context, ConditionToken.Type.OR)) {
            node = new OrNode(node, parseAnd(context));
        }
        return node;
    }

    private static ConditionNode parseAnd(ParseContext context) {
        ConditionNode node = parseUnary(context);
        while (match(context, ConditionToken.Type.AND)) {
            node = new AndNode(node, parseUnary(context));
        }
        return node;
    }

    private static ConditionNode parseUnary(ParseContext context) {
        if (match(context, ConditionToken.Type.NOT)) {
            return new NotNode(parseUnary(context));
        }
        return parsePrimary(context);
    }

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

    private static boolean match(ParseContext context, ConditionToken.Type type) {
        if (peek(context).type() == type) {
            context.pos++;
            return true;
        }
        return false;
    }

    private static void expect(ParseContext context, ConditionToken.Type type) {
        if (!match(context, type)) {
            throw error(context, "Expected " + type);
        }
    }

    private static ConditionToken peek(ParseContext context) {
        return context.tokens.get(context.pos);
    }

    private static ConditionToken previous(ParseContext context) {
        return context.tokens.get(context.pos - 1);
    }

    private static RuntimeException error(ParseContext context, String msg) {
        return new RuntimeException(
                msg + " at token " + peek(context)
        );
    }

    static final class ParseContext {
        final List<ConditionToken> tokens;
        int pos = 0;

        ParseContext(List<ConditionToken> tokens) {
            this.tokens = tokens;
        }
    }
}
