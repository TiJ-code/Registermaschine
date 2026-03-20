package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.error.SyntaxErrorException;

import java.util.List;

public interface IParser {
    ISyntaxTree parse(List<IToken> tokens) throws SyntaxErrorException;
}
