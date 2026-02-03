package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.lexing.IToken;

import java.util.List;

public interface ILexer {
    List<IToken> tokenize(String sourceCode);
}
