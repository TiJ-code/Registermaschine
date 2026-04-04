package dk.tij.registermaschine.api.compilation;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledProgram;
import dk.tij.registermaschine.api.compilation.parsing.ISyntaxTree;
import dk.tij.registermaschine.api.instructions.IInstructionSet;

/**
 * Defines the contract for a compiler that transforms a syntax tree
 * into a {@link ICompiledProgram}.
 *
 * <p>The compiler is responsible for interpreting the structure of the
 * provided {@link ISyntaxTree} and producing a program representation
 * suitable for execution.</p>
 *
 * <p>An {@link IInstructionSet} is provided to resolve instruction semantics,
 * including opcode mapping and operand handling.</p>
 *
 * <p>This interface does not define how compilation is performed or what
 * intermediate representations are used.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public interface ICompiler {
    /**
     * Compiles a syntax tree into a compiled program.
     *
     * @param syntaxTree the syntax tree representing the program
     * @param set the instruction set used for instruction resolution
     * @return a compiled program
     */
    ICompiledProgram compile(ISyntaxTree syntaxTree, IInstructionSet set);
}
