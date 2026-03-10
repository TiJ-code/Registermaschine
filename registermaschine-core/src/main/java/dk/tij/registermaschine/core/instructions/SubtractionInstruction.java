package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

import java.util.Arrays;

/**
 * Instruction handler that performs integer subtraction.
 *
 * <p>This instruction subtracts all operands marked with the
 * {@link OperandConcept#OPERAND} concept sequentially and stores the resulting
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
public final class SubtractionInstruction extends AbstractInstruction {
    public SubtractionInstruction(byte opcode, int operandCount, ICondition condition) {
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
     * Executes the subtraction operation.
     *
     * <p>Operands with the concept {@link OperandConcept#OPERAND} are
     * subtracted in sequence (first minus second, minus third, etc.)
     * and the resulting value is written to the {@link OperandConcept#RESULT} operand.</p>
     *
     * <p>If the calculated value exceeds the 32-bit signed integer
     * range, the overflow flag is set.</p>
     *
     * @param context the execution context providing register access
     *                and processor state
     * @param operands the compiled operands participating in the
     *                 subtraction operation
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        ICompiledOperand destination = null;
        Long runningDifference = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
                continue;
            }

            int value = getValueFromOperand(context, op);

            if (runningDifference == null) {
                runningDifference = (long) value;
            } else {
                runningDifference -= value;
            }
        }

        if (destination != null && runningDifference != null) {
            boolean overFlow = (runningDifference > Integer.MAX_VALUE) ||
                    (runningDifference < Integer.MIN_VALUE);

            context.setFlags(runningDifference < 0, runningDifference == 0, overFlow);
            context.setRegister(destination.value(), runningDifference.intValue());
        }
    }
}
