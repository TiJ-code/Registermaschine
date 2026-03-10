package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.lexing.IToken;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.error.SyntaxErrorException;

import java.util.List;

/**
 * Defines the contract for a parser that transforms a sequence of
 * tokens into a syntax tree suitable for compilation.
 *
 * <p>The parser validates syntax rules and may throw a
 * {@link SyntaxErrorException} if the tokens do not form a valid program.</p>
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
     * @throws SyntaxErrorException if the tokens violate the language syntax
     */
    ISyntaxTree parse(List<IToken> tokens) throws SyntaxErrorException;
}
