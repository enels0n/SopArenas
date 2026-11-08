package net.enelson.soparenas.engine;

import net.enelson.soparenas.api.ArenaLocation;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

record PlayerSnapshot(ArenaLocation location, GameMode gameMode, ItemStack[] inventory, ItemStack[] armor,
                      ItemStack offHand, int level, float experience, double health, int food, float saturation,
                      boolean allowFlight, boolean flying) {
    static PlayerSnapshot capture(Player player) {
        return new PlayerSnapshot(ArenaLocation.from(player.getLocation()), player.getGameMode(),
                cloneItems(player.getInventory().getContents()), cloneItems(player.getInventory().getArmorContents()),
                cloneItem(player.getInventory().getItemInOffHand()), player.getLevel(), player.getExp(),
                player.getHealth(), player.getFoodLevel(), player.getSaturation(), player.getAllowFlight(), player.isFlying());
    }

    void restore(Player player) {
        player.getInventory().setContents(cloneItems(inventory));
        player.getInventory().setArmorContents(cloneItems(armor));
        player.getInventory().setItemInOffHand(cloneItem(offHand));
        player.setGameMode(gameMode);
        player.setLevel(level);
        player.setExp(experience);
        player.setHealth(Math.min(health, player.getMaxHealth()));
        player.setFoodLevel(food);
        player.setSaturation(saturation);
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight && flying);
        if (location.toLocation() != null) {
            player.teleport(location.toLocation());
        }
    }

    private static ItemStack[] cloneItems(ItemStack[] source) {
        ItemStack[] result = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) result[i] = cloneItem(source[i]);
        return result;
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
