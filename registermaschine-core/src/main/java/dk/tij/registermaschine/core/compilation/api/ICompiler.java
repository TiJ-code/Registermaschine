package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.config.InstructionSet;

public interface ICompiler {
    ICompiledProgram compile(ISyntaxTree syntaxTree, InstructionSet set);
}
