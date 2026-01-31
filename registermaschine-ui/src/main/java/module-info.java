module registermaschine {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.fxmisc.richtext;

    opens dk.tij.registermaschine.ui to javafx.fxml, javafx.graphics;

    exports dk.tij.registermaschine.ui;
}