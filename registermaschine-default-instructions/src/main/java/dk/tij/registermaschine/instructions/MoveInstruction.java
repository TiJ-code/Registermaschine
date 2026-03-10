package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

/**
 * Instruction that moves a value into a specified register.
 *
 * <p>This instruction supports two types of operands:</p>
 * <ul>
 *     <li>{@link OperandConcept#RESULT}: the destination register index</li>
 *     <li>{@link OperandConcept#OPERAND}: the source value, either a literal or
 *     fetched from a register</li>
 * </ul>
 *
 * <p>The value is resolved using {@link AbstractInstruction#getValueFromOperand(IExecutionContext, ICompiledOperand)}
 * operand evaluation. Conditional execution applies if a condition was specified
 * at instruction creation.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     MOV r5, 0  ; copy value from register 0 into register 5
 * </pre>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class MoveInstruction extends AbstractInstruction {
    public MoveInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    /**
     * Resolves the source value and stores it in the destination register
     *
     * @param context the execution context with registers
     * @param operands the compiled operands; one destination and one or more source operands
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        int value = 0;
        int destinationIndex = -1;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destinationIndex = op.value();
            } else {
                value = getValueFromOperand(context, op);
            }
        }

        if (destinationIndex != -1) {
            context.setRegister(destinationIndex, value);
        }
    }
}
