package me.wairesd.dcstackedcases.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player-specific data such as current page and open inventory.
 */
public class PlayerDataManager {
    // Stores the current page the player is viewing
    private final Map<UUID, Integer> pages = new HashMap<>();
    // Stores the custom inventory the player has open
    private final Map<UUID, Inventory> playerInventories = new HashMap<>();

    public void setPage(Player player, int page) {
        pages.put(player.getUniqueId(), page);
    }

    public int getPage(Player player) {
        return pages.getOrDefault(player.getUniqueId(), 0);
    }

    public void setInventory(Player player, Inventory inventory) {
        playerInventories.put(player.getUniqueId(), inventory);
    }

    public Inventory getInventory(Player player) {
        return playerInventories.get(player.getUniqueId());
    }

    public void removeInventory(Player player) {
        playerInventories.remove(player.getUniqueId());
    }
}