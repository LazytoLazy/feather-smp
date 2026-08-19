package com.feathersmp.plugin.listeners;

import com.feathersmp.plugin.FeatherManager;
import com.feathersmp.plugin.FeatherSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Enforces a max of 1 FeatherSMP feather across a player's offhand, main
 * inventory, and open crafting grid. Any of these actions could change how
 * many feathers a player is holding, so each schedules a next-tick check
 * (via FeatherManager#scheduleFeatherLimitCheck) once the action has
 * resolved - picking one up, moving one in an inventory, dragging one
 * across slots, or logging back in with more than one saved from before
 * this limit existed.
 */
public class FeatherLimitListener implements Listener {

    private final FeatherManager manager;

    public FeatherLimitListener(FeatherSMP plugin) {
        this.manager = plugin.getFeatherManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.scheduleFeatherLimitCheck(event.getPlayer());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            manager.scheduleFeatherLimitCheck(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.scheduleFeatherLimitCheck(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.scheduleFeatherLimitCheck(player);
        }
    }
}
