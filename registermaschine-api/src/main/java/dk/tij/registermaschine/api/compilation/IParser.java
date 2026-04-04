package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.error.SyntaxErrorException;

import java.util.List;

/**
 * Defines the contract for a parser that transforms a sequence of
 * {@link IToken tokens} into a {@link ISyntaxTree}.
 *
 * <p>The parser interprets the token stream according to the language
 * grammar and constructs a corresponding syntax tree.</p>
 *
 * <p>If the input does not conform to the expected syntax, a
 * {@link SyntaxErrorException} may be thrown.</p>
 *
 * <p>This interface does not define the structure of the resulting
 * syntax tree.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface IParser {
    /**
     * Parses a list of tokens into a syntax tree.
     *
     * @param tokens the tokens to parse
     * @return the resulting syntax tree
     * @throws SyntaxErrorException if the input violates syntax rules
     */
    ISyntaxTree parse(List<IToken> tokens) throws SyntaxErrorException;
}
