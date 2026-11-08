package net.enelson.soparenas.engine;

import net.enelson.soparenas.SopArenasPlugin;
import net.enelson.soparenas.api.ArenaDefinition;
import net.enelson.soparenas.api.ArenaState;
import net.enelson.soparenas.api.GameMode;
import net.enelson.soparenas.api.MatchContext;
import net.enelson.soparenas.api.MatchFinishReason;
import net.enelson.soparenas.api.MatchRequest;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ArenaEngine {
    private static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_-]{1,48}");
    private final SopArenasPlugin plugin;
    private final ArenaRepository arenas;
    private final TemporaryWorldService worlds;
    private final Map<String, GameMode> modes = new LinkedHashMap<>();
    private final Map<String, MatchInstance> matches = new LinkedHashMap<>();
    private final Map<UUID, MatchInstance> matchesByPlayer = new LinkedHashMap<>();

    public ArenaEngine(SopArenasPlugin plugin, ArenaRepository arenas) {
        this.plugin = plugin;
        this.arenas = arenas;
        this.worlds = new TemporaryWorldService(plugin);
    }

    public void registerGameMode(GameMode mode) {
        String id = normalize(mode.id());
        if (modes.putIfAbsent(id, mode) != null) throw new IllegalStateException("Game mode already registered: " + id);
    }

    public void unregisterGameMode(String id) {
        String normalized = normalize(id);
        for (MatchInstance match : matches.values().stream()
                .filter(value -> normalize(value.gameMode().id()).equals(normalized)).toList()) {
            finish(match.id(), MatchFinishReason.PLUGIN_DISABLE);
        }
        modes.remove(normalized);
    }

    public MatchContext start(MatchRequest request) {
        String id = normalize(request.instanceId());
        if (matches.containsKey(id)) throw new IllegalStateException("Match already exists: " + id);
        GameMode mode = modes.get(normalize(request.gameModeId()));
        if (mode == null) throw new IllegalStateException("Unknown game mode: " + request.gameModeId());
        ArenaDefinition arena = request.arenaId() == null ? null : arenas.get(request.arenaId());
        Collection<Player> requestedPlayers = request.players() == null ? Collections.emptyList() : request.players();
        for (Player player : requestedPlayers) {
            if (matchesByPlayer.containsKey(player.getUniqueId())) throw new IllegalStateException(player.getName() + " is already in a match.");
        }

        World world = worlds.create(id);
        MatchInstance match = new MatchInstance(id, mode, arena, world,
                request.options() == null ? Collections.emptyMap() : request.options());
        try {
            mode.prepare(match);
            match.state(ArenaState.WAITING);
            matches.put(id, match);
            for (Player player : requestedPlayers) addPlayer(match, player);
            match.state(ArenaState.RUNNING);
            mode.start(match);
            return match;
        } catch (Exception exception) {
            matches.remove(id);
            for (UUID playerId : match.snapshots().keySet()) matchesByPlayer.remove(playerId);
            restorePlayers(match);
            worlds.delete(world);
            throw new IllegalStateException("Could not start match " + id, exception);
        }
    }

    public void finish(String id, MatchFinishReason reason) {
        MatchInstance match = matches.remove(normalize(id));
        if (match == null) return;
        match.state(ArenaState.ENDING);
        try { match.gameMode().finish(match, reason); }
        catch (Exception exception) { plugin.getLogger().severe("Game mode finish failed for " + id + ": " + exception.getMessage()); }
        restorePlayers(match);
        worlds.delete(match.world());
        match.state(ArenaState.FINISHED);
    }

    public void leave(Player player) {
        MatchInstance match = matchesByPlayer.remove(player.getUniqueId());
        if (match == null) return;
        PlayerSnapshot snapshot = match.snapshots().remove(player.getUniqueId());
        try { match.gameMode().playerLeft(match, player); } catch (Exception ignored) {}
        if (snapshot != null && plugin.getConfig().getBoolean("restore-player-state", true)) snapshot.restore(player);
        if (match.snapshots().isEmpty() && plugin.getConfig().getBoolean("finish-match-when-empty", true)) {
            finish(match.id(), MatchFinishReason.EMPTY);
        }
    }

    public MatchContext match(Player player) { return matchesByPlayer.get(player.getUniqueId()); }
    public MatchContext match(String id) { return matches.get(normalize(id)); }
    public Collection<? extends MatchContext> matches() { return Collections.unmodifiableCollection(matches.values()); }
    public ArenaRepository arenas() { return arenas; }
    public void shutdown() {
        for (String id : matches.keySet().toArray(String[]::new)) finish(id, MatchFinishReason.PLUGIN_DISABLE);
    }

    private void addPlayer(MatchInstance match, Player player) throws Exception {
        match.snapshots().put(player.getUniqueId(), PlayerSnapshot.capture(player));
        matchesByPlayer.put(player.getUniqueId(), match);
        Location spawn = match.spawn().toLocation();
        if (spawn == null || spawn.getWorld() != match.world()) spawn = match.world().getSpawnLocation().add(0.5, 1, 0.5);
        if (!player.teleport(spawn)) throw new IllegalStateException("Could not teleport " + player.getName());
        match.gameMode().playerJoined(match, player);
    }

    private void restorePlayers(MatchInstance match) {
        for (Map.Entry<UUID, PlayerSnapshot> entry : match.snapshots().entrySet()) {
            matchesByPlayer.remove(entry.getKey());
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && plugin.getConfig().getBoolean("restore-player-state", true)) entry.getValue().restore(player);
        }
        match.snapshots().clear();
    }

    private String normalize(String id) {
        String normalized = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        if (!SAFE_ID.matcher(normalized).matches()) throw new IllegalArgumentException("Invalid id: " + id);
        return normalized;
    }
}
