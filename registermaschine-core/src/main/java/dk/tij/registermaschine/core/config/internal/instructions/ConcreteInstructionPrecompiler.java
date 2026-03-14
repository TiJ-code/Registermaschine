package dk.tij.registermaschine.core.config.internal.instructions;

import dk.tij.registermaschine.core.config.api.instructions.IInstructionPrecompiler;
import dk.tij.registermaschine.core.config.model.ConfigInstruction;
import dk.tij.registermaschine.core.instructions.api.ChainedInstruction;

public class ConcreteInstructionPrecompiler implements IInstructionPrecompiler<ChainedInstruction> {
    private static final ConcreteInstructionPrecompiler INSTANCE = new ConcreteInstructionPrecompiler();

    private ConcreteInstructionPrecompiler() {}
    
    @Override
    public final ChainedInstruction precompile(ConfigInstruction instruction) {
        return new ChainedInstruction(
                instruction.operands().size(),
                instruction.condition(),
                ConcreteInstructionStepPrecompiler.instance().precompile(instruction)
        );
    }

    public static IInstructionPrecompiler<ChainedInstruction> instance() {
        return INSTANCE;
    }
}
