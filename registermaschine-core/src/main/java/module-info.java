module dk.tij.registermaschine.core {
    exports dk.tij.registermaschine.core.exception;
    exports dk.tij.registermaschine.core.cpu;

    exports dk.tij.registermaschine.core.runtime;
    exports dk.tij.registermaschine.core.runtime.api;

    exports dk.tij.registermaschine.core.config;
    exports dk.tij.registermaschine.core.config.api;

    exports dk.tij.registermaschine.core.instructions;
    exports dk.tij.registermaschine.core.instructions.api;

    exports dk.tij.registermaschine.core.conditions;
    exports dk.tij.registermaschine.core.conditions.internal;
    exports dk.tij.registermaschine.core.conditions.api;

    exports dk.tij.registermaschine.core.compilation.api;
    exports dk.tij.registermaschine.core.compilation.api.parsing;
    exports dk.tij.registermaschine.core.compilation.api.compiling;
    exports dk.tij.registermaschine.core.compilation.api.lexing;

    requires java.xml;
}