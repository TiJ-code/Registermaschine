module dk.tij.registermaschine.api {
    exports tij.registermaschine.api.compilation;
    exports tij.registermaschine.api.compilation.parsing;
    exports tij.registermaschine.api.compilation.lexing;
    exports tij.registermaschine.api.compilation.compiling;
    exports tij.registermaschine.api.compilation.pre;
    exports tij.registermaschine.api.runtime;
    exports tij.registermaschine.api.conditions;
    exports tij.registermaschine.api.instructions;
    exports tij.registermaschine.api.config;
    exports tij.registermaschine.api.error;
    exports tij.registermaschine.api.plugin;
    exports tij.registermaschine.api.log;
    exports tij.registermaschine.api.config.model;
    exports tij.registermaschine.api.devices;

    requires java.xml;
    requires java.logging;
}