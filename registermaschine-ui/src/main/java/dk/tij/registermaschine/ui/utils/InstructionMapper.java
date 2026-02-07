package dk.tij.registermaschine.ui.utils;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InstructionMapper {
    public static List<Map<String, String>> toDocList(IInstructionSet set) {
        return set.getInstructions().stream()
                .map(i -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", i.mnemonic());
                    map.put("description", i.description());
                    return map;
                })
                .toList();
    }

    public static List<String> toKeywords(IInstructionSet set) {
        return set.getInstructions().stream()
                .map(ConfigInstruction::mnemonic)
                .toList();
    }
}
