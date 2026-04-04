package dk.tij.registermaschine.api.plugin;

public interface IPlugin {
    void onEnable();
    void onDisable();
    String getName();
    String getDescription();
    String getVersion();
    String getAuthor();
}
