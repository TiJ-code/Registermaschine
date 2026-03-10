package dk.tij.registermaschine.core.instructions;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

/**
 * Instruction that reads an input value from the execution context
 * and stores it in a register.
 *
 * <p>The first operand must be a
 * {@link dk.tij.registermaschine.core.compilation.api.compiling.OperandConcept#RESULT}
 * operand specifying the destination register.</p>
 *
 * <p>The instruction blocks execution until input is provided
 * via {@link IExecutionContext#provideInput(int)}.</p>
 *
 * <p>If the input operation is interrupted, the instruction
 * re-interrupts the current thread but does not set the register.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class InputInstruction extends AbstractInstruction {
    public InputInstruction(byte opcode, int operandCount, ICondition condition) {
        super(opcode, operandCount, condition);
    }

    @Override
    public void executeInstruction(IExecutionContext context, ICompiledOperand[] operands) {
        try {
            context.setRegister(operands[0].value(), context.input());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
