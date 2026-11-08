package net.enelson.soparenas.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public interface MatchContext {
    String id();
    ArenaState state();
    World world();
    ArenaDefinition arena();
    Collection<Player> players();
    Map<String, Object> options();
    <T> T option(String key, Class<T> type);
    void spawn(ArenaLocation location);
    ArenaLocation spawn();
}
