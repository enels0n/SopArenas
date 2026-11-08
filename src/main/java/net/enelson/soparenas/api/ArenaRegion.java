package net.enelson.soparenas.api;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public record ArenaRegion(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public static ArenaRegion between(Location first, Location second) {
        if (!first.getWorld().equals(second.getWorld())) {
            throw new IllegalArgumentException("Region corners must be in the same world.");
        }
        return new ArenaRegion(first.getWorld().getName(), Math.min(first.getBlockX(), second.getBlockX()),
                Math.min(first.getBlockY(), second.getBlockY()), Math.min(first.getBlockZ(), second.getBlockZ()),
                Math.max(first.getBlockX(), second.getBlockX()), Math.max(first.getBlockY(), second.getBlockY()),
                Math.max(first.getBlockZ(), second.getBlockZ()));
    }

    public static ArenaRegion read(ConfigurationSection section) {
        return new ArenaRegion(section.getString("world", ""), section.getInt("min-x"), section.getInt("min-y"),
                section.getInt("min-z"), section.getInt("max-x"), section.getInt("max-y"), section.getInt("max-z"));
    }

    public void write(ConfigurationSection section) {
        section.set("world", world);
        section.set("min-x", minX);
        section.set("min-y", minY);
        section.set("min-z", minZ);
        section.set("max-x", maxX);
        section.set("max-y", maxY);
        section.set("max-z", maxZ);
    }

    public boolean contains(Location location) {
        return location.getWorld() != null && world.equals(location.getWorld().getName())
                && location.getBlockX() >= minX && location.getBlockX() <= maxX
                && location.getBlockY() >= minY && location.getBlockY() <= maxY
                && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
    }
}
