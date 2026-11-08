package net.enelson.soparenas.api;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public record MatchRequest(String instanceId, String gameModeId, String arenaId,
                           Collection<Player> players, Map<String, Object> options) {
}
