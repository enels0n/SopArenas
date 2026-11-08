package net.enelson.soparenas.api;

import org.bukkit.entity.Player;

public interface GameMode {
    String id();
    default void prepare(MatchContext context) throws Exception {}
    default void start(MatchContext context) throws Exception {}
    default void playerJoined(MatchContext context, Player player) throws Exception {}
    default void playerLeft(MatchContext context, Player player) throws Exception {}
    default void finish(MatchContext context, MatchFinishReason reason) throws Exception {}
}
