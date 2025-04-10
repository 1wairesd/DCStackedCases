package com.wairesd.dcstackedcases.listener;

import com.wairesd.dcstackedcases.DCStackedCasesAddon;
import com.wairesd.dcstackedcases.config.ConfigManager;
import com.wairesd.dcstackedcases.config.LanguageMessages;
import com.wairesd.dcstackedcases.manager.InventoryGuiManager;
import com.wairesd.dcstackedcases.manager.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    private final DCStackedCasesAddon plugin;
    private final PlayerDataManager playerDataManager;
    private final InventoryGuiManager inventoryGuiManager;
    private final LanguageMessages lang;
    private final ConfigManager config;

    public InventoryListener(DCStackedCasesAddon plugin, PlayerDataManager playerDataManager, InventoryGuiManager inventoryGuiManager, LanguageMessages lang, ConfigManager config) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.inventoryGuiManager = inventoryGuiManager;
        this.lang = lang;
        this.config = config;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory inv = playerDataManager.getInventory(p);
        if (inv == null || e.getClickedInventory() != inv) return;

        e.setCancelled(true);

        // Обрабатываем навигацию по страницам
        int slot = e.getSlot(), page = playerDataManager.getPage(p);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();
        String back = ChatColor.translateAlternateColorCodes('&', lang.getBackItemDisplayName());
        String forward = ChatColor.translateAlternateColorCodes('&', lang.getForwardItemDisplayName());
        int backSlot = config.getBackItemSlot(), forwardSlot = config.getForwardItemSlot(), itemsPerPage = config.getMenuSize() - 9;

        if (slot == backSlot && name.equals(back) && page > 0) {
            inventoryGuiManager.openGui(p, page - 1); // Переход на предыдущую страницу
        } else if (slot == forwardSlot && name.equals(forward)) {
            inventoryGuiManager.openGui(p, page + 1); // Переход на следующую страницу
        } else if (slot < itemsPerPage && config.isDebug()) {
            plugin.getLogger().info("Игрок " + p.getName() + " кликнул кейс");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (playerDataManager.getInventory(p) == e.getInventory()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        String title = ChatColor.translateAlternateColorCodes('&', lang.getMenuTitle().replace("%d", String.valueOf(playerDataManager.getPage(p) + 1)));
        if (inv.getSize() == config.getMenuSize() && e.getView().getTitle().equals(title)) {
            playerDataManager.setInventory(p, inv); // Регистрируем инвентарь игрока
            if (config.isDebug()) plugin.getLogger().info("Игрок " + p.getName() + " открыл страницу");
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getInventory().getSize() == config.getMenuSize()) {
            playerDataManager.removeInventory(p); // Удаляем инвентарь из данных игрока
            if (config.isDebug()) plugin.getLogger().info("Игрок " + p.getName() + " закрыл инвентарь");
        }
    }
}