package tij.registermaschine.compiler;

import java.util.Objects;
import java.util.regex.Pattern;

public record CompilerOptions(
        int registers,
        boolean allowLabels,
        String registerRegex
) {
    public CompilerOptions {
        if (registers < 0) {
            throw new IllegalArgumentException("Register count cannot be negative");
        }
        Objects.requireNonNull(registerRegex, "registerRegex");
        Pattern.compile(registerRegex);
    }
}
