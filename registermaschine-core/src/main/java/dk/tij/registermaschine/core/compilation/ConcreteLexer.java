package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.api.compilation.ILexer;
import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.core.compilation.internal.lexing.ConcreteToken;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.ArrayList;
import java.util.List;
import static dk.tij.registermaschine.api.compilation.lexing.TokenType.*;

/**
 * Concrete implementation of {@link ILexer} for the Registermaschine.
 *
 * <p>This lexer transforms a source code string into a list of {@link IToken}s
 * suitable for parsing. It handles instructions, registers, numbers, labels,
 * comments, and special address/immediate syntax. It also performs basic
 * validation and reports lexical errors for malformed tokens.</p>
 *
 * <p>Supported token types:</p>
 * <ul>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#INSTRUCTION}
 *     – recognised mnemonics from the {@link IInstructionSet}</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#REGISTER}
 *     – registers prefixed with 'r' (e.g., r0, r1)</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#NUMBER}
 *     – immediate values prefixed with '#' and optionally hexadecimal</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#LABEL}
 *     – symbolic addresses or labels</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#LABEL_DEF}
 *     – label definitions ending with ':'</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#COMMA}
 *     – comma separators between operands</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#EOL}
 *     – end-of-line markers</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#EOF}
 *     – end-of-file marker</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#COMMENT}
 *     – comments starting with ';'</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#UNKNOWN}
 *     – unrecognised characters</li>
 *     <li>{@link dk.tij.registermaschine.api.compilation.lexing.TokenType#ERROR}
 *     – malformed tokens (e.g., invalid immediate/address)</li>
 * </ul>
 *
 * <p>Lexer features:</p>
 * <ul>
 *     <li>Normalizes line endings to '\n'</li>
 *     <li>Tracks line and column positions for error reporting</li>
 *     <li>Supports hexadecimal addresses and immediates</li>
 *     <li>Handles symbolic labels based on {@link CoreConfig#ALLOW_LABELS}</li>
 *     <li>Validates register names using {@link CoreConfig#TOKEN_REGEX}</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConcreteLexer implements ILexer {
    private IInstructionSet instructionSet;
    private List<IToken> tokens;
    private int index, line, column;
    private String source;

    /**
     * Tokenises the given source code using the provided instruction set.
     *
     * @param sourceCode the source code string to tokenise
     * @param instructions the instruction set to recognise instruction tokens
     * @return a list of {@link IToken}s representing the lexed source code
     */
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

    /**
     * Reads an immediate value prefixed with '#' and handles decimal/hex formats.
     */
    private void readImmediate() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        if (!isNotAtEnd()) {
            tokens.add(new ConcreteToken(ERROR, "Expected number after '#'", line, startCol));
            return;
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

    /**
     * Reads a hexadecimal address prefixed with '@0x'.
     */
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

    /**
     * Reads a comment until the end of the line.
     */
    private void readComment() {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();

        while (isNotAtEnd() && peek() != '\n') {
            sb.append(advance());
        }

        tokens.add(new ConcreteToken(COMMENT, sb.toString(), line, startCol));
    }

    /**
     * Handles numbers without '#' prefix, producing an ERROR token.
     */
    private void readIllegalNumber(char first) {
        int startCol = column - 1;
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (isNotAtEnd() && Character.isLetterOrDigit(peek())) {
            sb.append(advance());
        }

        tokens.add(new ConcreteToken(ERROR, "Constants must start with '#': " + sb, line, startCol));
    }

    /**
     * Reads identifiers (instructions, registers, labels, or label definitions).
     */
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

    /**
     * Checks if the given text matches the configured register regex.
     */
    private boolean isRegister(String text) {
        if (text.length() < 2) return false;
        if (text.charAt(0) != 'r') return false;

        return text.matches(CoreConfig.TOKEN_REGEX.get(REGISTER));
    }

    /**
     *  Checks whether the given character is a valid hexadecimal digit.
     */
    private boolean isHexDigit(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * Advances the lexer index by one character and returns it.
     */
    private char advance() {
        char c = source.charAt(index++);
        column++;
        return c;
    }

    /**
     * Returns the next character without advancing.
     */
    private char peek() {
        return source.charAt(index);
    }

    /**
     * Returns {@code true} if there are remaining characters to process.
     */
    private boolean isNotAtEnd() {
        return index < source.length();
    }
}
