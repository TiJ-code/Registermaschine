package tij.registermaschine.api.registry;

import tij.registermaschine.api.compilation.ICompiler;
import tij.registermaschine.api.compilation.ILexer;
import tij.registermaschine.api.compilation.IParser;
import tij.registermaschine.api.instructions.IInstructionSet;

public record ComponentRegistries(
        IFactoryRegistry<ILexer> lexers,
        IFactoryRegistry<IParser> parsers,
        IFactoryRegistry<ICompiler> compilers,
        IFactoryRegistry<IInstructionSet> instructionSets
) {
}
