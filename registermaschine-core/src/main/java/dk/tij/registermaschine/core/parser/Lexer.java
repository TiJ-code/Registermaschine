package dk.tij.registermaschine.core.parser;

import dk.tij.registermaschine.core.config.Config;

import java.util.ArrayList;
import java.util.List;
import static dk.tij.registermaschine.core.parser.Token.Type.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int index = 0, line = 1, column = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (isNotAtEnd()) {
            char c = advance();

            switch (c) {
                case ' ', '\t':
                    break;

                case '\n':
                    tokens.add(new Token(EOL, "\\n", line, column - 1));
                    line++;
                    column = 1;
                    break;

                case ';':
                    readComment();
                    break;

                case '#':
                    readImmediate();
                    break;

                default:
                    if (Character.isDigit(c)) {
                        readNumber(c);
                    } else if (Character.isLetter(c)) {
                        readIdentifier(c);
                    } else {
                        tokens.add(new Token(UNKNOWN, String.valueOf(c), line, column));
                    }
            }
        }

        if (!tokens.isEmpty() && tokens.getLast().type != EOL)
            tokens.add(new Token(EOL, "\\n", line, column));

        tokens.add(new Token(EOF, "", line, column));
        return tokens;
    }

    private void readImmediate() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

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

    private void readNumber(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (isNotAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        tokens.add(new Token(NUMBER, sb.toString(), line, startCol));
    }

    private void readIdentifier(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        if (first == '#') {
            while (isNotAtEnd() && Character.isDigit(peek())) {
                sb.append(advance());
            }
            tokens.add(new Token(NUMBER, sb.toString(), line, startCol));
            return;
        }

        while (isNotAtEnd() && Character.isLetterOrDigit(peek())) {
            sb.append(advance());
        }

        String text = sb.toString().toLowerCase();
        if (text.matches(Config.TOKEN_REGEX.get(REGISTER))) {
            tokens.add(new Token(REGISTER, text, line, startCol));
        } else {
            tokens.add(new Token(INSTRUCTION, text.toLowerCase(), line, startCol));
        }
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
