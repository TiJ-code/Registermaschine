package dk.tij.registermaschine.core.parser;

import dk.tij.registermaschine.core.parser.ast.ASTNode;
import dk.tij.registermaschine.core.parser.ast.InstructionNode;
import dk.tij.registermaschine.core.parser.ast.OperandNode;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currentIndex = 0;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode> parse() {
        List<ASTNode> nodes = new ArrayList<>();

        while (isNotAtEnd()) {
            ASTNode node = parseInstruction();
            if (node != null) nodes.add(node);
        }

        return nodes;
    }

    private ASTNode parseInstruction() {
        while (match(Token.Type.EOL) || match(Token.Type.UNKNOWN)) {}

        if (peek().type == Token.Type.EOF) return null;

        Token instr = consume(Token.Type.INSTRUCTION, "Expected instruction");
        List<OperandNode> operands = new ArrayList<>();

        while (!check(Token.Type.EOL) && !check(Token.Type.EOF)) {
            operands.add(parseOperand());
        }

        match(Token.Type.EOL);
        return new InstructionNode(instr.value, operands, instr.line);
    }

    private OperandNode parseOperand() {
        while (match(Token.Type.UNKNOWN));

        if (match(Token.Type.REGISTER)) {
            Token t = previous();
            return new OperandNode(t.value, true, t.line);
        }

        if (match(Token.Type.NUMBER)) {
            Token t = previous();
            return new OperandNode(t.value, false, t.line);
        }

        throw error(peek(), "Invalid operand");
    }

    private boolean match(Token.Type type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private Token consume(Token.Type type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private boolean check(Token.Type type) {
        return isNotAtEnd() && peek().type == type;
    }

    private Token advance() {
        if (isNotAtEnd()) currentIndex++;
        return previous();
    }

    boolean isNotAtEnd() {
        return peek().type != Token.Type.EOF;
    }

    private Token peek() {
        return tokens.get(currentIndex);
    }

    private Token previous() {
        return tokens.get(currentIndex - 1);
    }

    private RuntimeException error(Token token, String msg) {
        return new RuntimeException(
                "Parser error at line " + token.line + ", col " + token.column + ": " + msg
        );
    }
}
