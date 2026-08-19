package com.feathersmp.plugin.commands;

import com.feathersmp.plugin.FeatherManager;
import com.feathersmp.plugin.FeatherSMP;
import com.feathersmp.plugin.FeatherTier;
import com.feathersmp.plugin.FeatherType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /feather list
 * /feather give <player> <type> [basic|upgraded] [amount]   (feathersmp.give, default op)
 * /feather items <player> [amount]                          (feathersmp.items, default op) - gives Upgrade Tokens
 * /feather reload                                            (feathersmp.reload, default op)
 */
public class FeatherCommand implements CommandExecutor, TabCompleter {

    private final FeatherSMP plugin;
    private final FeatherManager manager;

    public FeatherCommand(FeatherSMP plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFeatherManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> handleList(sender);
            case "give" -> handleGive(sender, args);
            case "items" -> handleItems(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleList(CommandSender sender) {
        if (sender instanceof Player player) {
            com.feathersmp.plugin.gui.FeatherCatalogGUI.open(player, manager);
            return;
        }

        // Console/command blocks can't open an inventory GUI - fall back to text.
        sender.sendMessage(color("&b--- FeatherSMP Feathers ---"));
        for (FeatherType type : FeatherType.values()) {
            sender.sendMessage(color(type.getDisplayName() + " &7(" + type.getId() + ")"));
            sender.sendMessage(color("  &aWeak: &f" + type.getAbilityName(com.feathersmp.plugin.AbilityTier.WEAK)
                    + " &7- " + type.getAbilityDescription(com.feathersmp.plugin.AbilityTier.WEAK)));
            sender.sendMessage(color("  &cStrong: &f" + type.getAbilityName(com.feathersmp.plugin.AbilityTier.STRONG)
                    + " &7- " + type.getAbilityDescription(com.feathersmp.plugin.AbilityTier.STRONG)));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("feathersmp.give")) {
            sender.sendMessage(prefix() + color(message("no-permission", "&cYou don't have permission to do that.")));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&cUsage: /feather give <player> <type> [basic|upgraded] [amount]"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(prefix() + color(message("player-not-found", "&cPlayer not found.")));
            return;
        }

        FeatherType type = FeatherType.fromId(args[2]);
        if (type == null) {
            sender.sendMessage(prefix() + color(message("invalid-type", "&cUnknown feather type. Use /feather list to see valid types.")));
            return;
        }

        FeatherTier tier = FeatherTier.BASIC;
        int nextArgIndex = 3;
        if (args.length >= 4 && (args[3].equalsIgnoreCase("basic") || args[3].equalsIgnoreCase("upgraded"))) {
            tier = args[3].equalsIgnoreCase("upgraded") ? FeatherTier.UPGRADED : FeatherTier.BASIC;
            nextArgIndex = 4;
        }

        int amount = 1;
        if (args.length > nextArgIndex) {
            try {
                amount = Math.max(1, Integer.parseInt(args[nextArgIndex]));
            } catch (NumberFormatException ignored) {
                // fall back to 1
            }
        }

        ItemStack item = manager.createFeather(type, tier);
        item.setAmount(amount);
        target.getInventory().addItem(item);

        String displayName = color(type.getDisplayName());
        sender.sendMessage(prefix() + color(message("gave-feather", "&aGave {amount}x {feather} &ato {player}.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{feather}", displayName)
                .replace("{player}", target.getName())));

        target.sendMessage(prefix() + color(message("received-feather", "&aYou received {amount}x {feather}&a.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{feather}", displayName)));
    }

    private void handleItems(CommandSender sender, String[] args) {
        if (!sender.hasPermission("feathersmp.items")) {
            sender.sendMessage(prefix() + color(message("no-permission", "&cYou don't have permission to do that.")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /feather items <player> [amount]"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(prefix() + color(message("player-not-found", "&cPlayer not found.")));
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {
                // fall back to 1
            }
        }

        ItemStack token = manager.createUpgradeToken();
        token.setAmount(amount);
        target.getInventory().addItem(token);

        sender.sendMessage(prefix() + color(message("gave-feather", "&aGave {amount}x {feather} &ato {player}.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{feather}", color("&dFeather Upgrade Token"))
                .replace("{player}", target.getName())));

        target.sendMessage(prefix() + color(message("received-feather", "&aYou received {amount}x {feather}&a.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{feather}", color("&dFeather Upgrade Token"))));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("feathersmp.reload")) {
            sender.sendMessage(prefix() + color(message("no-permission", "&cYou don't have permission to do that.")));
            return;
        }
        plugin.reloadConfig();
        sender.sendMessage(prefix() + color(message("reloaded", "&aFeatherSMP config reloaded.")));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&b--- FeatherSMP ---"));
        sender.sendMessage(color("&7/feather list &f- list all feather types and their abilities"));
        if (sender.hasPermission("feathersmp.give")) {
            sender.sendMessage(color("&7/feather give <player> <type> [basic|upgraded] [amount] &f- give a feather"));
        }
        if (sender.hasPermission("feathersmp.items")) {
            sender.sendMessage(color("&7/feather items <player> [amount] &f- give Upgrade Tokens"));
        }
        if (sender.hasPermission("feathersmp.reload")) {
            sender.sendMessage(color("&7/feather reload &f- reload the config"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("list", "give", "items", "reload"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("items"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(Arrays.stream(FeatherType.values()).map(FeatherType::getId).collect(Collectors.toList()), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return filter(Arrays.asList("basic", "upgraded"), args[3]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }

    private String message(String key, String fallback) {
        return plugin.getConfig().getString("messages." + key, fallback);
    }

    private String prefix() {
        return color(plugin.getConfig().getString("prefix", "&8[&bFeatherSMP&8] &r"));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
