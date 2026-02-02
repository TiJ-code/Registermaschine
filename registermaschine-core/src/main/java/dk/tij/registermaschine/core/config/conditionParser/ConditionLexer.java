package dk.tij.registermaschine.core.config.conditionParser;

import dk.tij.registermaschine.core.config.conditionParser.nodes.ConditionToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConditionLexer {
    private ConditionLexer() {}

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

    private static ConditionToken readIdentifier(String source, AtomicInteger currentIndex) {
        int start = currentIndex.get();

        while (currentIndex.get() < source.length() &&
               (Character.isJavaIdentifierPart(source.charAt(currentIndex.get())) || source.charAt(currentIndex.get()) == '.'))
            currentIndex.incrementAndGet();

        return new ConditionToken(ConditionToken.Type.IDENTIFIER, source.substring(start, currentIndex.get()));
    }
}
