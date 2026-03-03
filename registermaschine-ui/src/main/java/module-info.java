module dk.tij.registermaschine.ui {
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires dk.tij.registermaschine.core;
    requires dk.tij.jissuesystem;

    opens dk.tij.registermaschine.ui to javafx.graphics;
    opens dk.tij.registermaschine.ui.ui to javafx.graphics, javafx.web;
    opens dk.tij.registermaschine.ui.utils to javafx.web;
}