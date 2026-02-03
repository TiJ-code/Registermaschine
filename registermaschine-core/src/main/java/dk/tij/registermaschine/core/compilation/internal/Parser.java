package dk.tij.registermaschine.core.compilation.internal;

import dk.tij.registermaschine.core.compilation.AbstractSyntaxTree;
import dk.tij.registermaschine.core.compilation.api.IParser;
import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTreeNode;
import dk.tij.registermaschine.core.compilation.parsing.InstructionNode;
import dk.tij.registermaschine.core.compilation.parsing.OperandNode;
import dk.tij.registermaschine.core.compilation.lexing.Token;

import java.util.ArrayList;
import java.util.List;

public final class Parser implements IParser {
    private static List<IToken> tokens;
    private static int currentIndex;

    @Override
    public ISyntaxTree parse(List<IToken> tokenList) {
        currentIndex = 0;
        tokens = tokenList;

        List<ISyntaxTreeNode> nodes = new ArrayList<>();

        while (isNotAtEnd()) {
            ISyntaxTreeNode node = parseInstruction();
            if (node != null) nodes.add(node);
        }

        return new AbstractSyntaxTree(nodes);
    }

    private static ISyntaxTreeNode parseInstruction() {
        while (match(Token.Type.EOL) || match(Token.Type.UNKNOWN)) {}

        if (peek().type() == Token.Type.EOF) return null;

        IToken instr = consume(Token.Type.INSTRUCTION, "Expected instruction");
        List<OperandNode> operands = new ArrayList<>();

        while (!check(Token.Type.EOL) && !check(Token.Type.EOF)) {
            operands.add(parseOperand());
        }

        match(Token.Type.EOL);
        return new InstructionNode(instr.value(), operands, instr.line());
    }

    private static OperandNode parseOperand() {
        while (match(Token.Type.UNKNOWN));

        if (match(Token.Type.REGISTER)) {
            IToken t = previous();
            return new OperandNode(t.value(), true, t.line());
        }

        if (match(Token.Type.NUMBER)) {
            IToken t = previous();
            return new OperandNode(t.value(), false, t.line());
        }

        throw error(peek(), "Invalid operand");
    }

    private static boolean match(Token.Type type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private static IToken consume(Token.Type type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private static boolean check(Token.Type type) {
        return isNotAtEnd() && peek().type() == type;
    }

    private static IToken advance() {
        if (isNotAtEnd()) currentIndex++;
        return previous();
    }

    private static boolean isNotAtEnd() {
        return peek().type() != Token.Type.EOF;
    }

    private static IToken peek() {
        return tokens.get(currentIndex);
    }

    private static IToken previous() {
        return tokens.get(currentIndex - 1);
    }

    private static RuntimeException error(IToken token, String msg) {
        return new RuntimeException(
                "Parser error at line " + token.line() + ", col " + token.column() + ": " + msg
        );
    }
}
