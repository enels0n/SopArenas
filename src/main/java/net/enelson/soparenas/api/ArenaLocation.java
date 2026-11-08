package net.enelson.soparenas.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public record ArenaLocation(String world, double x, double y, double z, float yaw, float pitch) {
    public static ArenaLocation from(Location location) {
        return new ArenaLocation(location.getWorld().getName(), location.getX(), location.getY(),
                location.getZ(), location.getYaw(), location.getPitch());
    }

    public static ArenaLocation read(ConfigurationSection section) {
        return new ArenaLocation(section.getString("world", ""), section.getDouble("x"),
                section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
    }

    public void write(ConfigurationSection section) {
        section.set("world", world);
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
        section.set("yaw", yaw);
        section.set("pitch", pitch);
    }

    public Location toLocation() {
        World loadedWorld = Bukkit.getWorld(world);
        return loadedWorld == null ? null : new Location(loadedWorld, x, y, z, yaw, pitch);
    }
}
