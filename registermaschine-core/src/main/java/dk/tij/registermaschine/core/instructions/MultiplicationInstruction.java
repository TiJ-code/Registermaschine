package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

import java.util.Arrays;

/**
 * Instruction handler that performs integer multiplication.
 *
 * <p>This instruction multiplies all operands marked with the
 * {@link OperandConcept#OPERAND} concept and stores the resulting
 * value in the operand marked with {@link OperandConcept#RESULT}.</p>
 *
 * <p>The operation supports both register and immediate operands.
 * Operand values are resolved using
 * {@link AbstractInstruction#getValueFromOperand(IExecutionContext, ICompiledOperand)}</p>
 *
 * <p>The instruction also updates the processor flags in the {@link IExecutionContext}:</p>
 * <ul>
 *     <li><b>Negative flag</b> - set if the result is negative</li>
 *     <li><b>Zero flag</b> - set if the result is {@code 0}</li>
 *     <li><b>Overflow flag</b> - set if the result exceeds the 32-bit
 *     signed integer range</li>
 * </ul>
 *
 * <p>Overflow detection is performed using a {@code long} accumulator
 * before the final result is cast to {@code int}.</p>
 *
 * <p>At least one {@link OperandConcept#RESULT} operand must be present.
 * Validation fails otherwise.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class MultiplicationInstruction extends AbstractInstruction {
    public MultiplicationInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    /**
     * Ensures that the instruction contains at least one
     * {@link OperandConcept#RESULT} operand.
     *
     * @param operands the operands supplied for execution
     * @throws RuntimeException if no result operand is present
     */
    @Override
    public void validate(ICompiledOperand[] operands) {
        super.validate(operands);
        if (Arrays.stream(operands).noneMatch(o -> o.concept() == OperandConcept.RESULT))
            throw new RuntimeException(String.format("Instruction Handler %s expects 1 result operand",
                    this.getClass().getSimpleName()));
    }

    /**
     * Executes the multiplication operation.
     *
     * <p>All operands with the concept {@link OperandConcept#OPERAND}
     * are multiplied and the resulting value is written to the
     * {@link OperandConcept#RESULT} operand.</p>
     *
     * <p>If the calculated value exceeds the 32-bit signed integer
     * range, the overflow flag is set.</p>
     *
     * @param context the execution context providing register access
     *                and processor state
     * @param operands the compiled operands participating in the
     *                 multiplication operation
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        long product = 1;
        ICompiledOperand destination = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
            } else if (op.concept() == OperandConcept.OPERAND) {
                product *= getValueFromOperand(context, op);
            }
        }

        boolean overFlow = (product > Integer.MAX_VALUE) ||
                           (product < Integer.MIN_VALUE);

        if (destination != null) {
            context.setFlags(product < 0, product == 0, overFlow);
            context.setRegister(destination.value(), (int) product);
        }
    }
}
