package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.ILexer;
import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.lexing.Token;
import dk.tij.registermaschine.core.config.Config;

import java.util.ArrayList;
import java.util.List;
import static dk.tij.registermaschine.core.compilation.api.lexing.TokenType.*;

public final class ConcreteLexer implements ILexer {
    private List<IToken> tokens;
    private int index, line, column;
    private String source;

    @Override
    public List<IToken> tokenize(String sourceCode) {
        source = sourceCode.replaceAll("\r\n", "\n");
        index = 0;
        line = 1;
        column = 1;
        tokens = new ArrayList<>();

        while (isNotAtEnd()) {
            char c = advance();

            switch (c) {
                case ' ', '\t' -> {}

                case '\n' -> {
                    tokens.add(new Token(EOL, "\\n", line, column - 1));
                    line++;
                    column = 1;
                }

                case ',' -> tokens.add(new Token(COMMA, ",", line, column - 1));
                case ';' -> readComment();

                case '#' -> readImmediate();

                default -> {
                    if (Character.isDigit(c)) {
                        readIllegalNumber(c);
                    } else if (Character.isLetter(c)) {
                        readIdentifier(c);
                    } else {
                        tokens.add(new Token(UNKNOWN, String.valueOf(c), line, column));
                    }
                }
            }
        }

        if (!tokens.isEmpty() && tokens.getLast().type() != EOL)
            tokens.add(new Token(EOL, "\\n", line, column));

        tokens.add(new Token(EOF, null, line, column));
        return tokens;
    }

    private void readImmediate() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        if (isNotAtEnd() && !Character.isDigit(peek())) {
            tokens.add(new Token(ERROR, "Expected number after '#'", line, startCol));
            return;
        }

        while (isNotAtEnd() && Character.isDigit(peek()))
            sb.append(advance());

        tokens.add(new Token(NUMBER, sb.toString(), line, startCol));
    }

    private void readComment() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        while (isNotAtEnd() && peek() != '\n') {
            sb.append(advance());
        }

        tokens.add(new Token(COMMENT, sb.toString(), line, startCol));
    }

    private void readIllegalNumber(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (isNotAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        tokens.add(new Token(ERROR, "Constants must start with '#': " + sb, line, startCol));
    }

    private void readIdentifier(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (isNotAtEnd() && Character.isLetterOrDigit(peek())) {
            sb.append(advance());
        }

        String text = sb.toString();

        // label definition
        if (isNotAtEnd() && peek() == ':') {
            advance();
            tokens.add(new Token(LABEL_DEF, text, line, startCol));
            return;
        }

        String textLower = text.toLowerCase();

        if (isRegister(textLower)) {
            tokens.add(new Token(REGISTER, textLower, line, startCol));
            return;
        }

        if (Config.INSTRUCTIONS.contains(textLower)) {
            tokens.add(new Token(INSTRUCTION, textLower, line, startCol));
            return;
        }

        tokens.add(new Token(LABEL, text, line, startCol));
    }

    private boolean isRegister(String text) {
        if (text.length() < 2) return false;
        if (text.charAt(0) != 'r') return false;

        return text.matches(Config.TOKEN_REGEX.get(REGISTER));
    }

    private char advance() {
        char c = source.charAt(index++);
        column++;
        return c;
    }

    private char peek() {
        return source.charAt(index);
    }

    private boolean isNotAtEnd() {
        return index < source.length();
    }
}
