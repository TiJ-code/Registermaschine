package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.api.IParser;
import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.lexing.TokenType;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteSyntaxTree;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteInstructionNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteLabelNode;
import dk.tij.registermaschine.core.compilation.internal.parsing.ConcreteOperandNode;
import dk.tij.registermaschine.core.error.SyntaxErrorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete parser implementation for the Registermaschine.
 *
 * <p>This parser transforms a flat list of {@link IToken tokens} produced by
 * a lexer into a structured {@link ISyntaxTree syntax tree}. Each instruction
 * or label in the source code becomes a node within the syntax tree, with operands
 * attached to their respective instructions.</p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Handles instructions, labels, and operands</li>
 *     <li>Skips comments and empty lines</li>
 *     <li>Throws {@link SyntaxErrorException} for invalid tokens or missing operands</li>
 *     <li>Supports comma-separated operands</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConcreteParser implements IParser {
    /**
     * List of tokens to parse
     */
    private List<IToken> tokens;
    /**
     * Current index within the token list
     */
    private int currentIndex;

    /**
     * Parses a list of {@link IToken tokens} into a {@link ISyntaxTree}.
     *
     * @param tokenList the tokens produced by the lexer
     * @return a syntax tree representing the source code
     * @throws SyntaxErrorException if the source code contains errors
     */
    @Override
    public ISyntaxTree parse(List<IToken> tokenList) throws SyntaxErrorException {
        currentIndex = 0;
        tokens = tokenList;

        List<ISyntaxTreeNode> nodes = new ArrayList<>();

        while (isNotAtEnd()) {
            ISyntaxTreeNode node = parseInstruction();
            if (node != null) nodes.add(node);
        }

        return new ConcreteSyntaxTree(nodes);
    }

    /**
     * Parses a single instruction or label from the token stream.
     *
     * @return a {@link ISyntaxTreeNode} representing an instruction or label, or null if EOF
     * @throws SyntaxErrorException if the instruction or operands are invalid
     */
    private ISyntaxTreeNode parseInstruction() {
        while (match(TokenType.EOL) || match(TokenType.COMMENT)) {}

        if (peek().type() == TokenType.EOF) return null;

        if (peek().type() == TokenType.ERROR) {
            throw error(peek(), peek().value());
        }

        if (match(TokenType.LABEL_DEF)) {
            IToken label = previous();
            return new ConcreteLabelNode(label.value(), label.line());
        }

        IToken instr = consume(TokenType.INSTRUCTION, "Expected instruction");

        List<ConcreteOperandNode> operands = new ArrayList<>();

        while (!check(TokenType.EOL) && !check(TokenType.EOF)) {
            operands.add(parseOperand());
            match(TokenType.COMMA);
        }

        match(TokenType.EOL);

        return new ConcreteInstructionNode(instr.value(), operands, instr.line());
    }

    /**
     * Parses a single operand from the token stream.
     *
     * <p>An operand may be a register, immediate value, or label reference.</p>
     *
     * @return a {@link ConcreteOperandNode} representing the operand
     * @throws SyntaxErrorException if the token is not a valid operand
     */
    private ConcreteOperandNode parseOperand() {
        while (match(TokenType.COMMENT)) {}

        if (match(TokenType.REGISTER)) {
            IToken t = previous();
            return new ConcreteOperandNode(t.value(), true, false, t.line());
        }

        if (match(TokenType.NUMBER)) {
            IToken t = previous();
            return new ConcreteOperandNode(t.value(), false, false, t.line());
        }

        if (match(TokenType.LABEL)) {
            IToken t = previous();
            return new ConcreteOperandNode(t.value(), false, true, t.line());
        }
        
        if (match(TokenType.ERROR)) {
            IToken t = previous();
            throw error(t, t.value());
        }

        throw error(peek(), "Expected operand");
    }

    /**
     * Returns true if the next token matches the given type and advances the index.
     */
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Consumes the next token if it matches the expected type, or throws an error. #
     */
    private IToken consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    /**
     * Checks if the next token matches the given type without consuming it.
     */
    private boolean check(TokenType type) {
        return isNotAtEnd() && peek().type() == type;
    }

    /**
     * Advances the current index and returns the previous token.
     */
    private IToken advance() {
        if (isNotAtEnd()) currentIndex++;
        return previous();
    }

    /**
     * Returns true if we have not reached the end of the token list.
     */
    private boolean isNotAtEnd() {
        return peek().type() != TokenType.EOF;
    }

    /**
     *  Peeks at the current token without advancing.
     */
    private IToken peek() {
        return tokens.get(currentIndex);
    }

    /**
     * Returns the previously consumed token.
     */
    private IToken previous() {
        return tokens.get(currentIndex - 1);
    }

    /**
     * Constructs a {@link SyntaxErrorException} with token location info.
     */
    private SyntaxErrorException error(IToken token, String msg) {
        return new SyntaxErrorException(
                "at line " + token.line() + ", col " + token.column() + ": " + msg
        );
    }
}
