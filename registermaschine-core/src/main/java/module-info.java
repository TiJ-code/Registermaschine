module dk.tij.registermaschine.core {
    exports dk.tij.registermaschine.core;
    exports dk.tij.registermaschine.core.parser;
    exports dk.tij.registermaschine.core.parser.ast;
    exports dk.tij.registermaschine.core.instructions;
    exports dk.tij.registermaschine.core.conditions;
    exports dk.tij.registermaschine.core.config;
    exports dk.tij.registermaschine.core.implementation;

    requires java.xml;
    requires java.desktop;
}