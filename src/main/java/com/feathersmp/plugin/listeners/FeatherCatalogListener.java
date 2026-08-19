package com.feathersmp.plugin.listeners;

import com.feathersmp.plugin.gui.FeatherCatalogHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * The feather catalog (opened via /feather list) is a read-only display.
 * This blocks clicking, shift-clicking, number-key swapping, and dragging
 * items into or out of it, from either the top or the player's own
 * inventory while it's open.
 */
public class FeatherCatalogListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof FeatherCatalogHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof FeatherCatalogHolder) {
            event.setCancelled(true);
        }
    }
}
