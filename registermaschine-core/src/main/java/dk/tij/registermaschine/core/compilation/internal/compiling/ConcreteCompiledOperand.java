package dk.tij.registermaschine.core.compilation.internal.compiling;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;

/**
 * A concrete data holder for instruction operands.
 *
 * <p>Operands define the "what" or "where" of an instruction, such as a
 * constant value, a register index, or a memory address.</p>
 *
 * @param type    The interpretation type of the operand (e.g. {@link OperandType#REGISTER})
 * @param concept The semantic role of the operand (e.g. {@link OperandConcept#RESULT})
 * @param value   The constant value to be interpreted
 *
 * @since 1.0.0
 * @author TiJ
 */
public record ConcreteCompiledOperand(OperandType type, OperandConcept concept, int value)
        implements ICompiledOperand {}
