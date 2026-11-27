module tij.registermaschine.config {
    exports tij.registermaschine.core.config;

    requires transitive tij.registermaschine.api;
    requires transitive tij.registermaschine.engine;
    requires java.xml;
    requires java.logging;
}