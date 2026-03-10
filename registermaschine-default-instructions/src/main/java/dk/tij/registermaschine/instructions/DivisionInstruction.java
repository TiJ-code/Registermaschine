package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandConcept;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

import java.util.Arrays;

/**
 * Instruction handler that performs sequential integer division.
 *
 * <p>The first {@link OperandConcept#OPERAND} provides the initial
 * dividend. Each following operand divides the current running
 * quotient.</p>
 *
 * <p>The final result is written to the operand marked with
 * {@link OperandConcept#RESULT}.</p>
 *
 * <p>Example behaviour:</p>
 * <pre>
 *     DIV A B C
 *     Result = ((A / B) / C)
 * </pre>
 *
 * <p>The instruction updates the processor flags in the
 * {@link IExecutionContext}:</p>
 * <ul>
 *     <li><b>Negative flag</b> - set if the result is negative</li>
 *     <li><b>Zero flag</b> - set if the result equals {@code 0}</li>
 *     <li><b>Overflow flag</b> - set if a signed overflow condition occurs</li>
 * </ul>
 *
 * <p>If a divisor equals {@code 0}, execution is aborted:</p>
 * <ul>
 *     <li>The runtime prints an error message</li>
 *     <li>The exit code is set to {@code 1}</li>
 *     <li>The execution context is stopped</li>
 * </ul>
 *
 * <p>At least one {@link OperandConcept#RESULT} operand must be present.
 * Validation fails otherwise.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class DivisionInstruction extends AbstractInstruction {
    public DivisionInstruction(byte opcode, int operandCount, ICondition condition) {
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
     * Executes the sequential division operation.
     *
     * <p>The first operand becomes the initial dividend.
     * Each subsequent operand divides the running quotient.</p>
     *
     * <p>If a divisor equals {@code 0}, execution terminates
     * immediately and the execution context is stopped.</p>
     *
     * @param context the execution context providing register access
     *                and processor state
     * @param operands the compiled operands used in the division
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        ICompiledOperand destination = null;
        boolean overflow = false;
        Integer runningQuotient = null;

        for (ICompiledOperand op : operands) {
            if (op.concept() == OperandConcept.RESULT) {
                destination = op;
                continue;
            }

            int currentValue = getValueFromOperand(context, op);

            if (runningQuotient == null) {
                runningQuotient = currentValue;
            } else {
                if (currentValue == 0) {
                    System.err.println("Runtime Error: Division by zero!");
                    context.setExitCode((byte) 1);
                    context.stopExecution();
                    return;
                }

                if (runningQuotient == Integer.MIN_VALUE && currentValue == 1) {
                    overflow = true;
                    runningQuotient = Integer.MIN_VALUE;
                } else {
                    runningQuotient /= currentValue;
                }
            }
        }

        if (destination != null && runningQuotient != null) {
            context.setFlags(runningQuotient < 0, runningQuotient == 0, overflow);
            context.setRegister(destination.value(), runningQuotient);
        }
    }
}
