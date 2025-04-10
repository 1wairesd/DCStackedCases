package com.wairesd.dcstackedcases.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Map<UUID, Integer> pages = new HashMap<>();
    private final Map<UUID, Inventory> inventories = new HashMap<>();

    public void setPage(Player p, int page) { pages.put(p.getUniqueId(), page); }
    public int getPage(Player p) { return pages.getOrDefault(p.getUniqueId(), 0); }
    public void setInventory(Player p, Inventory inv) { inventories.put(p.getUniqueId(), inv); }
    public Inventory getInventory(Player p) { return inventories.get(p.getUniqueId()); }
    public void removeInventory(Player p) { inventories.remove(p.getUniqueId()); }
}