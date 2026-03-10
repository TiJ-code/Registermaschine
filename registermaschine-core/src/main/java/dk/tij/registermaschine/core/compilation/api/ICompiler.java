package dk.tij.registermaschine.core.compilation.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.compilation.api.parsing.ISyntaxTree;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

/**
 * Defines the contract for a compiler that transforms a parsed syntax
 * tree into a {@link ICompiledProgram}.
 *
 * <p>The compiler uses an {@link IInstructionSet} to resolve mnemonics
 * and operands to opcodes and runtime-ready operands.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICompiler {
    /**
     * Compiles a syntax tree into a compiled program.
     *
     * @param syntaxTree the parsed syntax tree representing the program
     * @param set the instruction set used for opcode and operand resolution
     * @return a compiled program ready for execution
     */
    ICompiledProgram compile(ISyntaxTree syntaxTree, IInstructionSet set);
}
