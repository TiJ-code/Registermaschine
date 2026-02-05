package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.config.ConcreteInstructionSet;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

public interface ICompiler {
    ICompiledProgram compile(ISyntaxTree syntaxTree, IInstructionSet set);
}
