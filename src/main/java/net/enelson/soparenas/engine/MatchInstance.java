package net.enelson.soparenas.engine;

import net.enelson.soparenas.api.ArenaDefinition;
import net.enelson.soparenas.api.ArenaLocation;
import net.enelson.soparenas.api.ArenaState;
import net.enelson.soparenas.api.GameMode;
import net.enelson.soparenas.api.MatchContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

final class MatchInstance implements MatchContext {
    private final String id;
    private final GameMode gameMode;
    private final ArenaDefinition arena;
    private final World world;
    private final Map<String, Object> options;
    private final Map<UUID, PlayerSnapshot> players = new LinkedHashMap<>();
    private ArenaState state = ArenaState.PREPARING;
    private ArenaLocation spawn;

    MatchInstance(String id, GameMode gameMode, ArenaDefinition arena, World world, Map<String, Object> options) {
        this.id = id;
        this.gameMode = gameMode;
        this.arena = arena;
        this.world = world;
        this.options = new LinkedHashMap<>(options);
        this.spawn = ArenaLocation.from(world.getSpawnLocation().add(0.5, 1, 0.5));
    }

    GameMode gameMode() { return gameMode; }
    Map<UUID, PlayerSnapshot> snapshots() { return players; }
    void state(ArenaState value) { state = value; }
    @Override public String id() { return id; }
    @Override public ArenaState state() { return state; }
    @Override public World world() { return world; }
    @Override public ArenaDefinition arena() { return arena; }
    @Override public Collection<Player> players() {
        return Collections.unmodifiableList(players.keySet().stream().map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList()));
    }
    @Override public Map<String, Object> options() { return Collections.unmodifiableMap(options); }
    @Override public <T> T option(String key, Class<T> type) {
        Object value = options.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }
    @Override public void spawn(ArenaLocation location) { spawn = location; }
    @Override public ArenaLocation spawn() { return spawn; }
}
