package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

/**
 * Base class for all instruction implementations in the Regisermaschine.
 *
 * <p>An {@link AbstractInstruction} defines the executable behaviour of a
 * machine instruction. Concrete subclasses implement the
 * {@link #executeInstruction(IExecutionContext, ICompiledOperand[])} method
 * to perform the specific oepration.</p>
 *
 * <p>Each instruction instance is associated with:</p>
 * <ul>
 *     <li>An opcode identifying the instruction in compiled programs</li>
 *     <li>A fixed number of operands</li>
 *     <li>An optional execution {@link ICondition}</li>
 * </ul>
 *
 * <p>The {@link dk.tij.registermaschine.core.runtime.Executor} retrieves
 * instruction handlers from the instruction set and invokes them during
 * program execution.</p>
 *
 * <p>Before execution, the instruction may validate its operands and evaluate
 * its execution condition.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public abstract class AbstractInstruction {
    public final byte OpCode;
    protected final int operandCount;
    protected final ICondition condition;

    /**
     * Creates a new instruction definition.
     *
     * @param opcode the numeric opcode identifying the instruction
     * @param operandCount the number of operands required by this instruction
     * @param condition an optional condition controlling whether the instruction
     *                  should execute, or {@code null} if the instruction should
     *                  always execute
     */
    public AbstractInstruction(final byte opcode, final int operandCount, ICondition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    /**
     * Validates the operand array supplied to this instruction.
     *
     * <p>The default implementation verifies that the operand array exists
     * and contains at least the expected number of operands.</p>
     *
     * <p>Subclasses may override this method if additional validation rules
     * are required.</p>
     *
     * @param operands the operands supplied for execution
     * @throws RuntimeException if the operand count is insufficient
     */
    public void validate(ICompiledOperand[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new RuntimeException(String.format("Instruction %s expects %d operands.",
                    this.getClass().getSimpleName(), operandCount));
    }

    /**
     * Determines whether this instruction should execute in the current context.
     *
     * <p>If a condition was provided during construction, the condition is
     * evaluated against the {@link IExecutionContext}. If no condition is
     * present, the instruction will always execute.</p>
     *
     * @param context the current execution context
     * @return {@code true} if the instruction should execute
     */
    public boolean shouldExecute(IExecutionContext context) {
        if (condition == null) return true;
        return condition.test(context);
    }

    /**
     * Executes the instruction logic.
     *
     * <p>This method is called by the runtime executor when the instruction
     * is reached during program execution.</p>
     *
     * @param context the execution context containing the machine state
     * @param operands the compiled operands supplied ot the instruction
     */
    public abstract void executeInstruction(IExecutionContext context, ICompiledOperand[] operands);

    /**
     * Resolves the operational value of an operand based on its type.
     *
     * <p>This helper method provides a unified way to access operand data
     * regardless of whether the value originates from a register or from
     * an immediate literal.</p>
     *
     * <ul>
     *     <li><b>REGISTER:</b> {@code operand.value()} is interpreted as a
     *     register index and the value is fetched from the
     *     {@link IExecutionContext}.</li>
     *     <li><b>IMMEDIATE:</b> the literal {@code operand.value()} is returned
     *     directly.</li>
     * </ul>
     *
     * @param context the current execution environment providing register access
     * @param operand the compiled operand containing the type and raw value
     * @return the resolved integer value to be used by the instruction
     * @throws UnsupportedOperationException if the operand type is {@code LABEL},
     *                                       because labels represent control flow targets rather than numeric values
     */
    protected int getValueFromOperand(IExecutionContext context, ICompiledOperand operand) {
        return switch (operand.type()) {
            case REGISTER -> context.getRegister(operand.value());
            case IMMEDIATE -> operand.value();
            default -> throw new UnsupportedOperationException("Labels are not math!");
        };
    }

    /**
     * Returns a string representing of the instruction definition.
     *
     * @return a formatted string containing the opcode and operand count
     */
    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
