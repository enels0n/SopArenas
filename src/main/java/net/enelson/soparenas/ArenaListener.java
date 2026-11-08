package net.enelson.soparenas;

import net.enelson.soparenas.engine.ArenaEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

final class ArenaListener implements Listener {
    private final ArenaEngine engine;
    ArenaListener(ArenaEngine engine) { this.engine = engine; }
    @EventHandler public void onQuit(PlayerQuitEvent event) { engine.leave(event.getPlayer()); }
}
