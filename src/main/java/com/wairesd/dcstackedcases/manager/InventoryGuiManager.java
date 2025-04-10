package com.wairesd.dcstackedcases.manager;

import com.jodexindustries.donatecase.api.DCAPI;
import com.jodexindustries.donatecase.api.tools.DCTools;
import com.wairesd.dcstackedcases.DCStackedCasesAddon;
import com.wairesd.dcstackedcases.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InventoryGuiManager {
    private final DCStackedCasesAddon plugin;
    private final PlayerDataManager playerDataManager;
    private final DCAPI api;
    private final ConfigManager config;

    public InventoryGuiManager(DCStackedCasesAddon plugin, PlayerDataManager playerDataManager, ConfigManager config) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.api = DCAPI.getInstance();
        this.config = config;
    }

    public void openGui(Player p, int page) {
        String title = DCTools.rc(config.getCurrentLanguageMessages().getMenuTitle().replace("%d", String.valueOf(page + 1)));
        Inventory inv = Bukkit.createInventory(null, config.getMenuSize(), title);
        playerDataManager.setInventory(p, inv);
        populateInventory(p, inv, page);
        p.openInventory(inv);
    }

    public void populateInventory(Player p, Inventory inv, int page) {
        api.getCaseKeyManager().getAsync(p.getName()).thenAccept(keys -> {
            List<ItemStack> items = prepareItems(keys != null ? keys : new java.util.HashMap<>(), p);
            inv.clear();
            int itemsPerPage = inv.getSize() - 9;
            if (items.isEmpty()) {
                ItemStack noKeys = new ItemStack(Material.BARRIER);
                noKeys.getItemMeta().setDisplayName(DCTools.rc(config.getCurrentLanguageMessages().getNotKeys()));
                inv.setItem(inv.getSize() / 2, noKeys);
            } else {
                int start = page * itemsPerPage, end = Math.min(start + itemsPerPage, items.size());
                for (int i = start, slot = 0; i < end; i++, slot++) inv.setItem(slot, items.get(i));

                int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
                if (page > 0)
                    inv.setItem(config.getBackItemSlot(), createNavItem(Material.ARROW, config.getCurrentLanguageMessages().getBackItemDisplayName()));
                if (page < totalPages - 1)
                    inv.setItem(config.getForwardItemSlot(), createNavItem(Material.ARROW, config.getCurrentLanguageMessages().getForwardItemDisplayName()));
            }
            playerDataManager.setPage(p, page);
        });
    }

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(DCTools.rc(name));
        item.setItemMeta(meta);
        return item;
    }

    private List<ItemStack> prepareItems(Map<String, Integer> keys, Player p) {
        List<ItemStack> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : keys.entrySet()) {
            String caseName = entry.getKey();
            int totalKeys = entry.getValue();
            while (totalKeys > 0) {
                int stackSize = Math.min(totalKeys, 64);
                items.add(createCaseItem(caseName, stackSize, p));
                totalKeys -= stackSize;
            }
        }
        return items;
    }

    private ItemStack createCaseItem(String caseName, int stackSize, Player p) {
        String matStr = plugin.getMaterialsConfig().getMaterialString(caseName);
        ItemStack item = matStr.startsWith("HEAD:") ?
                new ItemStack(Material.PLAYER_HEAD) : new ItemStack(Material.getMaterial(matStr) != null ? Material.getMaterial(matStr) : Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        if (matStr.startsWith("HEAD:"))
            ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(matStr.substring(5).replace("%player_name%", p.getName())));

        String displayName = plugin.getMaterialsConfig().getDisplayName(caseName);
        meta.setDisplayName(DCTools.rc(displayName != null ? displayName : "&a" + caseName));

        List<String> lore = plugin.getMaterialsConfig().getLore(caseName, stackSize);
        if (lore != null && !lore.isEmpty())
            meta.setLore(DCTools.rc(lore));
        else
            meta.setLore(DCTools.rc(
                    Collections.singletonList(
                            DCTools.rc(config.getCurrentLanguageMessages().getDefaultLoreKeys() + ": " + stackSize)
                    )
            ));

        meta.getPersistentDataContainer().set(DCStackedCasesAddon.CASE_NAME_KEY, org.bukkit.persistence.PersistentDataType.STRING, caseName);
        item.setItemMeta(meta);
        item.setAmount(stackSize);
        return item;
    }
}