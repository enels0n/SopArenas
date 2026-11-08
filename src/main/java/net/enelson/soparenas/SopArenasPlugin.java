package net.enelson.soparenas;

import net.enelson.soparenas.engine.ArenaEngine;
import net.enelson.soparenas.engine.ArenaRepository;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SopArenasPlugin extends JavaPlugin {
    private ArenaRepository arenas;
    private ArenaEngine engine;

    @Override public void onEnable() {
        saveDefaultConfig();
        arenas = new ArenaRepository(this);
        arenas.load();
        engine = new ArenaEngine(this, arenas);
        getServer().getPluginManager().registerEvents(new ArenaListener(engine), this);
        PluginCommand command = getCommand("soparenas");
        if (command != null) command.setExecutor(new ArenaCommand(engine));
    }

    @Override public void onDisable() {
        if (engine != null) engine.shutdown();
        if (arenas != null) arenas.save();
    }

    public ArenaEngine engine() { return engine; }
}
