package net.enelson.soparenas.engine;

import net.enelson.soparenas.SopArenasPlugin;
import net.enelson.soparenas.api.ArenaDefinition;
import net.enelson.soparenas.api.ArenaLocation;
import net.enelson.soparenas.api.ArenaRegion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ArenaRepository {
    private static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_-]{1,48}");
    private final SopArenasPlugin plugin;
    private final File file;
    private final Map<String, ArenaDefinition> arenas = new LinkedHashMap<>();

    public ArenaRepository(SopArenasPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public void load() {
        if (!file.exists()) plugin.saveResource("arenas.yml", false);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        arenas.clear();
        for (String id : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(id);
            if (section == null) continue;
            ArenaDefinition arena = new ArenaDefinition(id.toLowerCase(Locale.ROOT));
            arena.displayName(section.getString("display-name", id));
            arena.minPlayers(section.getInt("min-players", 1));
            arena.maxPlayers(section.getInt("max-players", 4));
            arena.autoStartSeconds(section.getInt("auto-start-seconds", 15));
            ConfigurationSection points = section.getConfigurationSection("points");
            if (points != null) for (String key : points.getKeys(false)) {
                ConfigurationSection point = points.getConfigurationSection(key);
                if (point != null) arena.point(key, ArenaLocation.read(point));
            }
            ConfigurationSection regions = section.getConfigurationSection("regions");
            if (regions != null) for (String key : regions.getKeys(false)) {
                ConfigurationSection region = regions.getConfigurationSection(key);
                if (region != null) arena.region(key, ArenaRegion.read(region));
            }
            arenas.put(arena.id(), arena);
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (ArenaDefinition arena : arenas.values()) {
            ConfigurationSection section = yaml.createSection(arena.id());
            section.set("display-name", arena.displayName());
            section.set("min-players", arena.minPlayers());
            section.set("max-players", arena.maxPlayers());
            section.set("auto-start-seconds", arena.autoStartSeconds());
            ConfigurationSection points = section.createSection("points");
            arena.points().forEach((key, value) -> value.write(points.createSection(key)));
            ConfigurationSection regions = section.createSection("regions");
            arena.regions().forEach((key, value) -> value.write(regions.createSection(key)));
        }
        try { yaml.save(file); } catch (IOException exception) { throw new IllegalStateException("Could not save arenas.yml", exception); }
    }

    public ArenaDefinition create(String id) {
        String normalized = normalize(id);
        if (arenas.containsKey(normalized)) return null;
        ArenaDefinition arena = new ArenaDefinition(normalized);
        arenas.put(normalized, arena);
        save();
        return arena;
    }

    public boolean delete(String id) { boolean removed = arenas.remove(normalize(id)) != null; if (removed) save(); return removed; }
    public ArenaDefinition get(String id) { return id == null ? null : arenas.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<ArenaDefinition> all() { return Collections.unmodifiableCollection(arenas.values()); }

    private String normalize(String id) {
        String normalized = id == null ? "" : id.toLowerCase(Locale.ROOT);
        if (!SAFE_ID.matcher(normalized).matches()) throw new IllegalArgumentException("Invalid arena id: " + id);
        return normalized;
    }
}
