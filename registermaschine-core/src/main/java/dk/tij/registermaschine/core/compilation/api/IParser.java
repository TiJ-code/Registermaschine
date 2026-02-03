package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.error.SyntaxErrorException;

import java.util.List;

public interface IParser {
    ISyntaxTree parse(List<IToken> tokens) throws SyntaxErrorException;
}
