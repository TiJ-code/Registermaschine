module tij.registermaschine.engine {
    requires transitive tij.registermaschine.api;

    exports tij.registermaschine.engine.conditions;
    exports tij.registermaschine.engine.devices;
    exports tij.registermaschine.engine.instructions;
    exports tij.registermaschine.engine.memory;
    exports tij.registermaschine.engine.runtime;
}