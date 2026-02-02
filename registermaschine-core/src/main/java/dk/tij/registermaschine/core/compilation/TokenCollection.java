package dk.tij.registermaschine.core.compilation;

import dk.tij.registermaschine.core.compilation.lexing.Token;

import java.util.ArrayList;
import java.util.List;

public final class TokenCollection extends ArrayList<Token> implements Iterable<Token> {
    public TokenCollection(List<Token> tokens) {
        super(tokens);
    }
}
