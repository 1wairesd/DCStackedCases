package com.wairesd.dcstackedcases.listeners;

import com.wairesd.dcstackedcases.DCStackedCasesAddon;
import com.wairesd.dcstackedcases.managers.InventoryGuiManager;
import com.wairesd.dcstackedcases.managers.PlayerDataManager;
import com.wairesd.dcstackedcases.utils.CustomInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listener class to handle inventory-related events for the stacked cases GUI.
 */
public class InventoryListener implements Listener {
    private final DCStackedCasesAddon plugin;
    private final PlayerDataManager playerDataManager;
    private final InventoryGuiManager inventoryGuiManager;

    public InventoryListener(DCStackedCasesAddon plugin, PlayerDataManager playerDataManager, InventoryGuiManager inventoryGuiManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.inventoryGuiManager = inventoryGuiManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory expectedInv = playerDataManager.getInventory(player);
        // Check if the clicked inventory is the custom inventory for stacked cases
        if (expectedInv == null || event.getClickedInventory() != expectedInv) return;

        event.setCancelled(true); // Prevent default interaction

        int slot = event.getSlot();
        int page = playerDataManager.getPage(player);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();

        // Handle back button click
        if (slot == 45 && displayName.equals("§Back")) {
            if (page > 0) {
                inventoryGuiManager.openGui(player, page - 1);
            }
            // Handle forward button click
        } else if (slot == 53 && displayName.equals("§eForward")) {
            inventoryGuiManager.openGui(player, page + 1);
            // Handle case item click
        } else if (slot < 45) {
            if (meta.getPersistentDataContainer().has(DCStackedCasesAddon.CASE_NAME_KEY, PersistentDataType.STRING)) {
                String caseName = meta.getPersistentDataContainer().get(DCStackedCasesAddon.CASE_NAME_KEY, PersistentDataType.STRING);
                Bukkit.dispatchCommand(player, "dc open " + caseName);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory expectedInv = playerDataManager.getInventory(player);
        // Prevent dragging items in the custom inventory
        if (expectedInv == null || event.getInventory() != expectedInv) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof CustomInventoryHolder) {
            // Set the inventory and page when the custom inventory is opened
            playerDataManager.setInventory(player, event.getInventory());
            String title = event.getView().getTitle();
            if (title.startsWith("Stacked Cases - Page ")) {
                int page = Integer.parseInt(title.substring("Stacked Cases - Page ".length()));
                playerDataManager.setPage(player, page);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof CustomInventoryHolder) {
            // Clean up player data when the custom inventory is closed
            playerDataManager.removeInventory(player);
        }
    }
}