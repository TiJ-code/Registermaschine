package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.lexing.IToken;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.List;

public interface ILexer {
    List<IToken> tokenize(String sourceCode, IInstructionSet set);
}
