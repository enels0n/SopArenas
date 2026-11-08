package net.enelson.soparenas;

import net.enelson.soparenas.api.ArenaDefinition;
import net.enelson.soparenas.api.ArenaLocation;
import net.enelson.soparenas.api.MatchFinishReason;
import net.enelson.soparenas.engine.ArenaEngine;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class ArenaCommand implements CommandExecutor {
    private final ArenaEngine engine;
    ArenaCommand(ArenaEngine engine) { this.engine = engine; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0) return help(sender, label);
            switch (args[0].toLowerCase()) {
                case "create" -> {
                    if (args.length < 2) return help(sender, label);
                    sender.sendMessage(engine.arenas().create(args[1]) == null ? red("Arena already exists.") : green("Arena created."));
                }
                case "delete" -> {
                    if (args.length < 2) return help(sender, label);
                    sender.sendMessage(engine.arenas().delete(args[1]) ? green("Arena deleted.") : red("Arena not found."));
                }
                case "list" -> sender.sendMessage("Arenas: " + String.join(", ", engine.arenas().all().stream().map(ArenaDefinition::id).toList()));
                case "setpoint" -> {
                    if (!(sender instanceof Player player) || args.length < 3) return help(sender, label);
                    ArenaDefinition arena = engine.arenas().get(args[1]);
                    if (arena == null) { sender.sendMessage(red("Arena not found.")); return true; }
                    arena.point(args[2], ArenaLocation.from(player.getLocation()));
                    engine.arenas().save();
                    sender.sendMessage(green("Point saved."));
                }
                case "stop" -> {
                    if (args.length < 2) return help(sender, label);
                    engine.finish(args[1], MatchFinishReason.ADMIN);
                    sender.sendMessage(green("Match stopped if it existed."));
                }
                default -> { return help(sender, label); }
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            sender.sendMessage(red(exception.getMessage()));
        }
        return true;
    }

    private boolean help(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "/" + label + " create|delete|list|setpoint|stop");
        return true;
    }
    private String red(String text) { return ChatColor.RED + text; }
    private String green(String text) { return ChatColor.GREEN + text; }
}
