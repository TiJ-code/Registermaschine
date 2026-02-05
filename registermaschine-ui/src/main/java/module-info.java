module dk.tij.registermaschine.ui {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires dk.tij.registermaschine.core;

    opens dk.tij.registermaschine.ui to javafx.fxml, javafx.graphics;
}