package dk.tij.registermaschine.core.instructions.api;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledOperand;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;
import dk.tij.registermaschine.core.conditions.api.ICondition;

public abstract class AbstractInstruction {
    /**
     * @since 1.0.0
     * @deprecated Use {@link AbstractInstruction#OpCode()} instead.
     *             This will be made private soon.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public final byte OpCode;
    protected final int operandCount;
    protected final ICondition condition;

    public AbstractInstruction(final byte opcode, final int operandCount, ICondition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    public void validate(ICompiledOperand[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new RuntimeException(String.format("Instruction %s expects %d operands.",
                    this.getClass().getSimpleName(), operandCount));
    }

    public boolean shouldExecute(IExecutionContext context) {
        if (condition == null) return true;
        return condition.test(context);
    }

    public abstract void executeInstruction(IExecutionContext context, ICompiledOperand[] operands);

    /**
     * Resolves the operational value of an operand based on its type.
     *
     * <p>
     * This helper provides a uniform way to fetch data regardless of whether
     * it is stored in a register or provided as a literal constant.
     * </p>
     * <ul>
     * <li><b>REGISTER:</b> Interprets {@code operand.value()} as an index and
     * fetches the content from the {@code IExecutionContext}.</li>
     * <li><b>IMMEDIATE:</b> returns the literal {@code operand.value()} directly.</li>
     * </ul>
     * @param context The current execution environment providing register access.
     * @param operand The compiled operand containing the type and raw value / address.
     * @return The resolved integer value to be used in instruction logic.
     * @throws UnsupportedOperationException If the operand type is {@code LABEL},
     * as labels represent memory addresses rather than numeric data.
     */
    protected int getValueFromOperand(IExecutionContext context, ICompiledOperand operand) {
        return switch (operand.type()) {
            case REGISTER -> context.getRegister(operand.value());
            case IMMEDIATE -> operand.value();
            default -> throw new UnsupportedOperationException("Labels are not math!");
        };
    }

    /**
     * Returns the opcode of this instruction handler
     *
     * @return the opcode
     *
     * @since 2.0.0
     */
    public byte OpCode() {
        return OpCode;
    }

    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
