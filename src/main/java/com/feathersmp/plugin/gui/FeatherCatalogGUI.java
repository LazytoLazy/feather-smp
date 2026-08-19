package com.feathersmp.plugin.gui;

import com.feathersmp.plugin.FeatherManager;
import com.feathersmp.plugin.FeatherType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FeatherCatalogGUI {

    private static final int SIZE = 27;

    private FeatherCatalogGUI() {
    }

    public static void open(Player player, FeatherManager manager) {
        FeatherCatalogHolder holder = new FeatherCatalogHolder();
        Inventory inventory = org.bukkit.Bukkit.createInventory(
                holder, SIZE, ChatColor.translateAlternateColorCodes('&', "&8&lFeather Catalog"));
        holder.setInventory(inventory);

        int slot = 0;
        for (FeatherType type : FeatherType.values()) {
            ItemStack display = manager.createCatalogItem(type);
            inventory.setItem(slot, display);
            slot++;
        }

        player.openInventory(inventory);
    }
}
