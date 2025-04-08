package com.wairesd.dcstackedcases.managers;

import com.wairesd.dcstackedcases.DCStackedCasesAddon;
import com.wairesd.dcstackedcases.utils.CustomInventoryHolder;
import com.jodexindustries.donatecase.api.DCAPI;
import com.jodexindustries.donatecase.api.data.casedata.CaseData;
import com.jodexindustries.donatecase.spigot.tools.BukkitUtils; // Добавляем импорт
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InventoryGuiManager {
    private final DCStackedCasesAddon plugin;
    private final PlayerDataManager playerDataManager;
    private final DCAPI api;
    private static final String INVENTORY_TITLE = "Stacked Cases - Page %d";

    public InventoryGuiManager(DCStackedCasesAddon plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.api = DCAPI.getInstance();
    }

    /**
     * Opens the stacked cases GUI for a player on a specific page.
     */
    public void openGui(Player player, int page) {
        // Fetch player's case keys asynchronously to avoid blocking the main thread
        api.getCaseKeyManager().getAsync(player.getName()).thenAccept(keys -> {
            Plugin donateCase = BukkitUtils.getDonateCase();
            if (donateCase == null) {
                plugin.getLogger().severe("DonateCase plugin not found!");
                return;
            }

            List<ItemStack> items = prepareItems(keys);

            // Schedule GUI creation on the main thread (Bukkit requirement)
            Bukkit.getScheduler().runTask(donateCase, () -> {
                Inventory inv = createInventory(player, items, page);
                playerDataManager.setPage(player, page);
                playerDataManager.setInventory(player, inv);
                player.openInventory(inv);
            });
        });
    }

    // Остальные методы (prepareItems, createInventory, createCaseItem) остаются без изменений
    private List<ItemStack> prepareItems(Map<String, Integer> keys) {
        List<ItemStack> items = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : keys.entrySet()) {
            if (entry.getValue() <= 0) continue;
            CaseData caseData = api.getCaseManager().get(entry.getKey());
            if (caseData == null) continue;
            int amount = entry.getValue();
            while (amount > 0) {
                int stackSize = Math.min(amount, 64);
                items.add(createCaseItem(entry.getKey(), stackSize));
                amount -= stackSize;
            }
        }
        return items;
    }

    private Inventory createInventory(Player player, List<ItemStack> items, int page) {
        String title = String.format(INVENTORY_TITLE, page);
        Inventory inv = Bukkit.createInventory(new CustomInventoryHolder(), 54, title);

        if (items.isEmpty()) {
            ItemStack noCases = new ItemStack(Material.BARRIER);
            ItemMeta meta = noCases.getItemMeta();
            meta.setDisplayName("§cYou have no cases");
            noCases.setItemMeta(meta);
            inv.setItem(22, noCases);
        } else {
            int totalPages = (int) Math.ceil((double) items.size() / 45);
            int startIndex = page * 45;
            int endIndex = Math.min(startIndex + 45, items.size());
            for (int i = startIndex; i < endIndex; i++) {
                inv.setItem(i - startIndex, items.get(i));
            }

            if (page > 0) {
                ItemStack back = new ItemStack(Material.ARROW);
                ItemMeta backMeta = back.getItemMeta();
                backMeta.setDisplayName("§eBack");
                back.setItemMeta(backMeta);
                inv.setItem(45, back);
            }
            if (page < totalPages - 1) {
                ItemStack forward = new ItemStack(Material.ARROW);
                ItemMeta forwardMeta = forward.getItemMeta();
                forwardMeta.setDisplayName("§eForward");
                forward.setItemMeta(forwardMeta);
                inv.setItem(53, forward);
            }
        }
        return inv;
    }

    private ItemStack createCaseItem(String caseName, int stackSize) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a" + caseName);
        meta.setLore(Collections.singletonList("§Keys: " + stackSize));
        meta.getPersistentDataContainer().set(DCStackedCasesAddon.CASE_NAME_KEY, PersistentDataType.STRING, caseName);
        item.setItemMeta(meta);
        item.setAmount(stackSize);
        return item;
    }
}