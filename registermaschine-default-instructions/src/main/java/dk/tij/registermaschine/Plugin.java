package dk.tij.registermaschine;

import dk.tij.registermaschine.api.plugin.IPlugin;

public class Plugin implements IPlugin {
    private final String name, description, version, author;
    
    public Plugin(String name, String description, String version, String author) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getAuthor() {
        return author;
    }
}
