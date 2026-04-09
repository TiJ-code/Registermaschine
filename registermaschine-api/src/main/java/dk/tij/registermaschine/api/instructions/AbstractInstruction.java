package dk.tij.registermaschine.api.instructions;

import dk.tij.registermaschine.api.compilation.compiling.ICompiledOperand;
import dk.tij.registermaschine.api.compilation.compiling.OperandType;
import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.runtime.IExecutionContext;

/**
 * Base implementation of an instruction.
 *
 * <p>This class provides common functionality for instruction execution,
 * including operand validation, conditional execution, and operand
 * value resolution.</p>
 *
 * <p>Concrete subclass implement {@link #executeInstruction(IExecutionContext, ICompiledOperand[])}
 * to define the instruction's behaviour.</p>
 *
 * <p>Instructions may optionally be associated with an {@link ICondition}
 * that controls whether they execute.</p>
 *
 * <p>This class is a convenience base and not required for all instruction implementations.
 * However, if using default implementation of the compilation pipeline,
 * (e.g. {@link dk.tij.registermaschine.api.compilation.ICompiler}), it is required.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public abstract class AbstractInstruction {
    /**
     * Opcode identifying this instruction.
     */
    public final int OpCode;

    /**
     * Expected number of operands.
     */
    protected final int operandCount;

    /**
     * Optional execution condition.
     */
    protected final ICondition condition;

    /**
     * Creates a new instruction.
     *
     * @param opcode the opcode identifying the instruction
     * @param operandCount the required number of operands
     * @param condition optional execution condition, or {@code null}
     */
    public AbstractInstruction(final int opcode, final int operandCount, ICondition condition) {
        this.OpCode = opcode;
        this.operandCount = operandCount;
        this.condition = condition;
    }

    /**
     * Validates the provided operands.
     *
     * <p>The default implementation ensures that the operand array
     * contains at least the expected number of operands.</p>
     *
     * @param operands the operands to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(ICompiledOperand[] operands) {
        if (operands == null || operands.length < operandCount)
            throw new RuntimeException(String.format("Instruction %s expects %d operands.",
                    this.getClass().getSimpleName(), operandCount));
    }

    /**
     * Determines whether this instruction should execute.
     *
     * @param context the execution context
     * @return {@code true} if execution should proceed
     */
    public boolean shouldExecute(IExecutionContext context) {
        if (condition == null) return true;
        return condition.test(context);
    }

    /**
     * Executes the instruction logic.
     *
     * @param context the execution context
     * @param operands the operands for this instruction
     */
    public abstract void executeInstruction(IExecutionContext context, ICompiledOperand[] operands);

    /**
     * Resolves the runtime value of an operand.
     *
     * <p>The interpretation depends on {@link OperandType}.</p>
     *
     * @param context the execution context
     * @param operand the operand
     * @return the resolved value
     * @throws UnsupportedOperationException if the operand type is unsupported
     */
    protected int getValueFromOperand(IExecutionContext context, ICompiledOperand operand) {
        return switch (operand.type()) {
            case OperandType.REGISTER -> context.getRegister(operand.value());
            case OperandType.IMMEDIATE -> operand.value();
            default -> throw new UnsupportedOperationException(
                    "Cannot resolve %s to numeric value".formatted(operand.type())
            );
        };
    }

    @Override
    public String toString() {
        return String.format("%02x( %d )", OpCode, operandCount);
    }
}
