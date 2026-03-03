package dk.tij.registermaschine.core.runtime;

import java.util.Map;

public record ExecutionSnapshot(int programmeCounter,
                                Map<Integer, Integer> registers,
                                boolean negative,
                                boolean zero,
                                boolean overflow,
                                byte exitCode,
                                Integer output) {
}
