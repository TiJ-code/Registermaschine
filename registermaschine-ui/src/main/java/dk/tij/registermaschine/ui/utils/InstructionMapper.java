package dk.tij.registermaschine.ui.utils;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InstructionMapper {
    public static List<InstructionDefinition> toDocList(IInstructionSet set) {
        return set.getInstructions().stream()
                .map(i -> new InstructionDefinition(i.mnemonic(), i.description()))
                .toList();
    }

    public static List<String> toKeywords(IInstructionSet set) {
        return set.getInstructions().stream()
                .map(ConfigInstruction::mnemonic)
                .toList();
    }

    public record InstructionDefinition(String name, String description) {}
}
