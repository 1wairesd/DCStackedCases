package com.wairesd.dcstackedcases.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Marker class to identify custom inventories for stacked cases.
 */
public class CustomInventoryHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null; // No actual inventory stored here
    }
}