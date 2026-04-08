module dk.tij.registermaschine.api {
    exports dk.tij.registermaschine.api.compilation;
    exports dk.tij.registermaschine.api.compilation.parsing;
    exports dk.tij.registermaschine.api.compilation.lexing;
    exports dk.tij.registermaschine.api.compilation.compiling;
    exports dk.tij.registermaschine.api.compilation.pre;
    exports dk.tij.registermaschine.api.runtime;
    exports dk.tij.registermaschine.api.conditions;
    exports dk.tij.registermaschine.api.instructions;
    exports dk.tij.registermaschine.api.config;
    exports dk.tij.registermaschine.api.error;
    exports dk.tij.registermaschine.api.plugin;
    exports dk.tij.registermaschine.api.log;
    exports dk.tij.registermaschine.api.config.model;
    exports dk.tij.registermaschine.api.devices;

    requires java.xml;
    requires java.logging;
}