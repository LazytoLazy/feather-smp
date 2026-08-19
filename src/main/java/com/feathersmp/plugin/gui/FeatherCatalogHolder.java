package com.feathersmp.plugin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Tags an inventory as the FeatherSMP read-only catalog so the click/drag
 * listener can recognize it and block all item movement.
 */
public class FeatherCatalogHolder implements InventoryHolder {

    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
