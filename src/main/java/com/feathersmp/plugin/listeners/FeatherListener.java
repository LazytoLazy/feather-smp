package com.feathersmp.plugin.listeners;

import com.feathersmp.plugin.AbilityTier;
import com.feathersmp.plugin.FeatherManager;
import com.feathersmp.plugin.FeatherSMP;
import com.feathersmp.plugin.FeatherTier;
import com.feathersmp.plugin.FeatherType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FeatherListener implements Listener {

    private final FeatherSMP plugin;
    private final FeatherManager manager;

    public FeatherListener(FeatherSMP plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFeatherManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFeatherUse(PlayerInteractEvent event) {
        // Only handle the main hand so a dual-wielded off-hand item doesn't fire twice.
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        FeatherType type = manager.getFeatherType(item);
        if (type == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        AbilityTier ability = player.isSneaking() ? AbilityTier.STRONG : AbilityTier.WEAK;

        if (ability == AbilityTier.STRONG && manager.getFeatherTier(item) == FeatherTier.BASIC) {
            player.sendMessage(prefix() + color(plugin.getConfig().getString(
                    "messages.ability-locked",
                    "&cThat ability is locked. &7Combine this feather with an Upgrade Token in a crafting table."
            )));
            return;
        }

        if (manager.isOnCooldown(player, type, ability)) {
            long remaining = manager.getRemainingCooldown(player, type, ability);
            String message = plugin.getConfig().getString("messages.on-cooldown", "&cThat ability is on cooldown for &e{time}s&c.")
                    .replace("{time}", String.valueOf(remaining));
            player.sendMessage(prefix() + color(message));
            return;
        }

        manager.useAbility(player, type, ability);

        if (plugin.getConfig().getBoolean("consume-on-use", false)) {
            item.setAmount(item.getAmount() - 1);
        }

        String usedMessage = plugin.getConfig().getString("messages.used", "&aYou feel the power of &f{ability} &acourse through you!")
                .replace("{ability}", color(type.getAbilityName(ability)))
                .replace("{feather}", color(type.getDisplayName()));
        player.sendMessage(prefix() + color(usedMessage));
    }

    private String prefix() {
        return color(plugin.getConfig().getString("prefix", "&8[&bFeatherSMP&8] &r"));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
