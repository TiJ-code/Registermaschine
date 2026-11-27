package tij.registermaschine.api.registry;

import tij.registermaschine.api.compilation.ICompiler;
import tij.registermaschine.api.compilation.ILexer;
import tij.registermaschine.api.compilation.IParser;
import tij.registermaschine.api.compilation.compiling.ICompiledInstructionFactory;
import tij.registermaschine.api.compilation.lexing.ITokenFactory;
import tij.registermaschine.api.instructions.IInstructionSet;

/**
 * Groups the registries available for component factories.
 *
 * @param lexers the lexer factory registry
 * @param parsers the parser factory registry
 * @param compilers the compiler factory registry
 * @param instructionSets the instruction set factory registry
 *
 * @since 2.0.0
 * @author TiJ
 */
public record ComponentRegistries(
        IFactoryRegistry<ILexer> lexers,
        IFactoryRegistry<IParser> parsers,
        IFactoryRegistry<ICompiler> compilers,
        IFactoryRegistry<IInstructionSet> instructionSets,
        IFactoryRegistry<ITokenFactory> tokens,
        IFactoryRegistry<ICompiledInstructionFactory> compiledInstructions
) {
}
