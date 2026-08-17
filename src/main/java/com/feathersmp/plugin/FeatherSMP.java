package com.feathersmp.plugin;

import com.feathersmp.plugin.commands.FeatherCommand;
import com.feathersmp.plugin.listeners.FeatherListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class FeatherSMP extends JavaPlugin {

    private FeatherManager featherManager;
    private BukkitTask actionBarTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.featherManager = new FeatherManager(this);
        this.featherManager.registerRecipes();

        getServer().getPluginManager().registerEvents(new FeatherListener(this), this);

        FeatherCommand featherCommand = new FeatherCommand(this);
        getCommand("feather").setExecutor(featherCommand);
        getCommand("feather").setTabCompleter(featherCommand);

        startActionBarTask();

        getLogger().info("FeatherSMP enabled with " + FeatherType.values().length + " feathers.");
    }

    @Override
    public void onDisable() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        getLogger().info("FeatherSMP disabled.");
    }

    public FeatherManager getFeatherManager() {
        return featherManager;
    }

    /**
     * Every half second, shows any player holding a feather an action bar
     * with both ability names and their current cooldown/lock status.
     */
    private void startActionBarTask() {
        actionBarTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                String text = featherManager.buildActionBar(player, mainHand);
                if (text != null) {
                    Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
                    player.sendActionBar(component);
                }
            }
        }, 0L, 10L);
    }
}
