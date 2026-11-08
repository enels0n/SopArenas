package net.enelson.soparenas.engine;

import net.enelson.soparenas.SopArenasPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

final class TemporaryWorldService {
    private final SopArenasPlugin plugin;

    TemporaryWorldService(SopArenasPlugin plugin) { this.plugin = plugin; }

    World create(String instanceId) {
        String worldName = plugin.getConfig().getString("temporary-world-prefix", "soparena_") + instanceId;
        Path folder = folder(worldName);
        if (Bukkit.getWorld(worldName) != null || Files.exists(folder)) {
            throw new IllegalStateException("Temporary world already exists: " + worldName);
        }
        World.Environment environment = parseEnvironment(plugin.getConfig().getString("temporary-world-environment", "NORMAL"));
        WorldType type = parseType(plugin.getConfig().getString("temporary-world-type", "FLAT"));
        World world = Bukkit.createWorld(new WorldCreator(worldName).environment(environment).type(type)
                .generateStructures(plugin.getConfig().getBoolean("temporary-world-generate-structures", false)));
        if (world == null) throw new IllegalStateException("Could not create temporary world " + worldName);
        world.setAutoSave(false);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        return world;
    }

    void delete(World world) {
        for (Player player : world.getPlayers()) {
            if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().getFirst() != world) {
                player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
            }
        }
        String name = world.getName();
        if (!Bukkit.unloadWorld(world, false)) {
            throw new IllegalStateException("Could not unload temporary world " + name);
        }
        Path folder = folder(name);
        if (!Files.exists(folder)) return;
        try (var paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete temporary world " + name, exception);
        }
    }

    private Path folder(String name) { return plugin.getServer().getWorldContainer().toPath().resolve(name); }
    private World.Environment parseEnvironment(String value) {
        try { return World.Environment.valueOf(value.toUpperCase()); } catch (Exception ignored) { return World.Environment.NORMAL; }
    }
    private WorldType parseType(String value) {
        try { return WorldType.valueOf(value.toUpperCase()); } catch (Exception ignored) { return WorldType.FLAT; }
    }
}
