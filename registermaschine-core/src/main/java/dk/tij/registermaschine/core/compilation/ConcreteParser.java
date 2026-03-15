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

public final class ConcreteParser implements IParser {
    private List<IToken> tokens;
    private int currentIndex;

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

        if (match(TokenType.ADDRESS)) {
            IToken t = previous();
            return new ConcreteOperandNode(t.value(), false, true, t.line());
        }
        
        if (match(TokenType.ERROR)) {
            IToken t = previous();
            throw error(t, t.value());
        }

        throw error(peek(), "Expected operand");
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private IToken consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private boolean check(TokenType type) {
        return isNotAtEnd() && peek().type() == type;
    }

    private IToken advance() {
        if (isNotAtEnd()) currentIndex++;
        return previous();
    }

    private boolean isNotAtEnd() {
        return peek().type() != TokenType.EOF;
    }

    private IToken peek() {
        return tokens.get(currentIndex);
    }

    private IToken previous() {
        return tokens.get(currentIndex - 1);
    }

    private SyntaxErrorException error(IToken token, String msg) {
        return new SyntaxErrorException(
                "at line " + token.line() + ", col " + token.column() + ": " + msg
        );
    }
}
