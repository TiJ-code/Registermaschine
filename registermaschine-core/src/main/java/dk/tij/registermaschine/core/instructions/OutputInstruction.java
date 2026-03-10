package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

/**
 * Instruction handler that outputs a value from the execution context.
 *
 * <p>This instruction takes a single operand and sends its value to the
 * {@link IExecutionContext#output(int)} method, allowing external systems
 * such as user interfaces, loggers, or monitors to capture the value.</p>
 *
 * <p>The operand value is resolved using
 * {@link AbstractInstruction#getValueFromOperand(IExecutionContext, ICompiledOperand)},
 * supporting both:</p>
 * <ul>
 *     <li>Register operands – the value is read from the specified register</li>
 *     <li>Immediate operands – the literal value is used directly</li>
 * </ul>
 *
 * <p>This instruction does not modify registers or processor flags.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class OutputInstruction extends AbstractInstruction {
    public OutputInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    /**
     * Executes the output operation.
     *
     * <p>The operand is resolved and the resulting value is sent to
     * {@link IExecutionContext#output(int)}.</p>
     *
     * @param context the execution context providing register access
     *                and output handling
     * @param operands the compiled operands containing the value to output
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        context.output(getValueFromOperand(context, operands[0]));
    }
}
