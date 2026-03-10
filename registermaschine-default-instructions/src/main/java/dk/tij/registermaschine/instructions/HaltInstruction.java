package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

/**
 * Instruction that halts program execution immediately.
 *
 * <p>The first operand specifies the program exit code to be set
 * in the {@link IExecutionContext} before stopping execution.</p>
 *
 * <p>This instruction ignores any remaining operands and
 * unconditionally stops the machine, setting its halted state.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     HALT 0 ; set exit code 0 and stop execution
 * </pre>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class HaltInstruction extends AbstractInstruction {
    public HaltInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    /**
     * Stops the execution context and sets the exit code.
     *
     * @param context the execution context being halted
     * @param operands the compiled operands, fist operand provides the exit code
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        context.setExitCode((byte)operands[0].value());
        context.stopExecution();
    }
}
