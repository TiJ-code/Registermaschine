package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.List;

/**
 * Defines the contract for a lexical analyser (lexer) that transforms
 * source code text into a list of tokens.
 *
 * <p>The lexer uses the provided {@link IInstructionSet} to recognise
 * valid instruction mnemonics, registers, numbers, and other token types.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ILexer {
    /**
     * Tokenises the given source code into a list of {@link IToken tokens}.
     *
     * @param sourceCode the source code to tokenise
     * @param set the instruction set used to recognise mnemonics and token types
     * @return a list of tokens representing the lexical structure of the source code
     */
    List<IToken> tokenize(String sourceCode, IInstructionSet set);
}
