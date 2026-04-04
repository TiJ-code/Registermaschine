package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

import java.util.List;

/**
 * Defines the contract for a lexical analyser (lexer) that transforms
 * source text into a sequence of {@link IToken tokens}.
 *
 * <p>The lexer is responsible for splitting the input into meaningful
 * lexical units such as identifiers, literals, and structural symbols.</p>
 *
 * <p>An {@link IInstructionSet} may be used to assist in recognising
 * instruction-specific elements, such as mnemonics.</p>
 *
 * <p>This interface does not define how tokenisation is performed.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ILexer {
    /**
     * Tokenises the given source text.
     *
     * @param sourceCode the input text to tokenise
     * @param set the instruction set used for instruction-specific recognition
     * @return a list of tokens representing the lexical structure of the input
     */
    List<IToken> tokenize(String sourceCode, IInstructionSet set);
}
