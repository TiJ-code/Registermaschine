package dk.tij.registermaschine.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.runtime.IExecutionContext;
import dk.tij.registermaschine.api.conditions.ICondition;

/**
 * Instruction that sets the programme counter to a specified value,
 * effectively performing a jump in program execution.
 *
 * <p>This instruction takes a single operand, which represents the target
 * programme counter to jump to. Conditional execution may apply if a condition
 * was specified during instruction creation.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *  JMP 0xA ; jump to instruction at index 10
 * </pre>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class JumpInstruction extends AbstractInstruction {

    public JumpInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, 1, condition);
    }

    /**
     * Updates the programme counter to the operand value.
     *
     * @param context the execution context containing the programme counter
     * @param operands the compiled operands; first operand is the jump target
     */
    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        context.setProgrammeCounter( operands[0].value() );
    }
}
