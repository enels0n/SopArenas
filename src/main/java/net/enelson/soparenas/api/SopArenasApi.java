package net.enelson.soparenas.api;

import net.enelson.soparenas.engine.ArenaEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class SopArenasApi {
    private SopArenasApi() {}

    public static ArenaEngine engine() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SopArenas");
        if (!(plugin instanceof net.enelson.soparenas.SopArenasPlugin sopArenas) || !plugin.isEnabled()) {
            throw new IllegalStateException("SopArenas is not enabled.");
        }
        return sopArenas.engine();
    }
}
