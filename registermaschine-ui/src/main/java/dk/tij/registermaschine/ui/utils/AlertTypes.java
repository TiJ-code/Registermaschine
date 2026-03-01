package dk.tij.registermaschine.ui.utils;

public enum AlertTypes {
    ERROR,
    WARNING,
    SUCCESS;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
