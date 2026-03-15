package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.api.compilation.ILexer;
import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.core.compilation.internal.lexing.ConcreteToken;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.ArrayList;
import java.util.List;
import static dk.tij.registermaschine.api.compilation.lexing.TokenType.*;

public final class ConcreteLexer implements ILexer {
    private IInstructionSet instructionSet;
    private List<IToken> tokens;
    private int index, line, column;
    private String source;

    @Override
    public List<IToken> tokenize(String sourceCode, IInstructionSet instructions) {
        instructionSet = instructions;
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
                    tokens.add(new ConcreteToken(EOL, "\\n", line, column - 1));
                    line++;
                    column = 1;
                }

                case ',' -> tokens.add(new ConcreteToken(COMMA, ",", line, column - 1));
                case ';' -> readComment();

                case '@' -> readAddress();
                case '#' -> readImmediate();

                default -> {
                    if (Character.isDigit(c)) {
                        readIllegalNumber(c);
                    } else if (Character.isLetter(c)) {
                        readIdentifier(c);
                    } else {
                        tokens.add(new ConcreteToken(UNKNOWN, String.valueOf(c), line, column));
                    }
                }
            }
        }

        if (!tokens.isEmpty() && tokens.getLast().type() != EOL)
            tokens.add(new ConcreteToken(EOL, "\\n", line, column));

        tokens.add(new ConcreteToken(EOF, null, line, column));
        return tokens;
    }

    private void readImmediate() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        if (!isNotAtEnd()) {
            tokens.add(new ConcreteToken(ERROR, "Expected number after '#'", line, startCol));
            return;
        }

        if (peek() == '-') {
            sb.append(advance());
        }

        if (peek() == '0') {
            char zero = advance();
            if (isNotAtEnd() && (peek() == 'x' || peek() == 'X')) {
                sb.append(zero);
                sb.append(advance());

                while (isNotAtEnd() && isHexDigit(peek())) {
                    sb.append(advance());
                }

                if (sb.length() == 2) {
                    tokens.add(new ConcreteToken(ERROR, "Expected hex digits after '0x'", line, startCol));
                    return;
                }

                tokens.add(new ConcreteToken(NUMBER, sb.toString(), line, startCol));
                return;
            } else {
                sb.append(zero);
            }
        }

        while (isNotAtEnd() && Character.isDigit(peek()))
            sb.append(advance());

        if (sb.isEmpty()) {
            tokens.add(new ConcreteToken(ERROR, "Expected number '#'", line, startCol));
            return;
        }

        tokens.add(new ConcreteToken(NUMBER, sb.toString(), line, startCol));
    }

    private void readAddress() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append('@');

        if (!isNotAtEnd() || peek() != '0') {
            tokens.add(new ConcreteToken(ERROR, "Addresses must be hexadecimal (starting with '@0x')", line, startCol));
            return;
        }
        sb.append(advance());

        if (!isNotAtEnd() || (peek() != 'x' && peek() != 'X')) {
            tokens.add(new ConcreteToken(ERROR, "Addresses must be hexadecimal (starting with '@0x')", line, startCol));
            return;
        }
        sb.append(advance());

        while (isNotAtEnd() && isHexDigit(peek())) {
            sb.append(advance());
        }

        if (sb.length() == 2) {
            tokens.add(new ConcreteToken(ERROR, "Missing hex value after '@0x'", line, startCol));
            return;
        }

        tokens.add(new ConcreteToken(LABEL, sb.toString(), line, startCol));
    }

    private void readComment() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        while (isNotAtEnd() && peek() != '\n') {
            sb.append(advance());
        }

        tokens.add(new ConcreteToken(COMMENT, sb.toString(), line, startCol));
    }

    private void readIllegalNumber(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (isNotAtEnd() && Character.isLetterOrDigit(peek())) {
            sb.append(advance());
        }

        tokens.add(new ConcreteToken(ERROR, "Constants must start with '#': " + sb, line, startCol));
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
            if (CoreConfig.ALLOW_LABELS) {
                tokens.add(new ConcreteToken(LABEL_DEF, text, line, startCol));
            } else {
                tokens.add(new ConcreteToken(ERROR, "Symbolic labels are disabled: " + text, line, startCol));
            }
            return;
        }

        String textLower = text.toLowerCase();

        if (isRegister(textLower)) {
            tokens.add(new ConcreteToken(REGISTER, textLower, line, startCol));
            return;
        }

        if (instructionSet.contains(textLower)) {
            tokens.add(new ConcreteToken(INSTRUCTION, textLower, line, startCol));
            return;
        }

        if (CoreConfig.ALLOW_LABELS) {
            tokens.add(new ConcreteToken(LABEL, text, line, startCol));
        } else {
            tokens.add(new ConcreteToken(ERROR, "Symbolic labels are disabled: " + text, line, startCol));
        }
    }

    private boolean isRegister(String text) {
        if (text.length() < 2) return false;
        if (text.charAt(0) != 'r') return false;

        return text.matches(CoreConfig.TOKEN_REGEX.get(REGISTER));
    }

    private boolean isHexDigit(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
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
