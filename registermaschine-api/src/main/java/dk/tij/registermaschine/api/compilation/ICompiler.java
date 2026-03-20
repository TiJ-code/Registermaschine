package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledProgram;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

public interface ICompiler {
    ICompiledProgram compile(ISyntaxTree syntaxTree, IInstructionSet set);
}
