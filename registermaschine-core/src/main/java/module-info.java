module dk.tij.registermaschine.core {
    exports dk.tij.registermaschine.core.exception;
    exports dk.tij.registermaschine.core.compilation;
    exports dk.tij.registermaschine.core.compilation.lexing;
    exports dk.tij.registermaschine.core.compilation.parsing;
    exports dk.tij.registermaschine.core.compilation.compiling;
    exports dk.tij.registermaschine.core.instructions;
    exports dk.tij.registermaschine.core.conditions;
    exports dk.tij.registermaschine.core.config;
    exports dk.tij.registermaschine.core.cpu;
    exports dk.tij.registermaschine.core.runtime;

    requires java.xml;
}