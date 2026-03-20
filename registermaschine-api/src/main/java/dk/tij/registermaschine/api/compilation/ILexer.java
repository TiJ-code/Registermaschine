package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.List;

public interface ILexer {
    List<IToken> tokenize(String sourceCode, IInstructionSet set);
}
