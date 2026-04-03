package dk.tij.registermaschine.api.instructions;

/**
 * @since 1.1.0
 * @author TiJ
 */
public interface IInstructionRegistry {
    //void register(Class<? extends AbstractInstruction> handlerClass);

    void prohibit(String className);

    boolean contains(String className);

    //AbstractInstruction instantiate(String className,
    //                                int opcode, int operandCount, ICondition condition);
}
