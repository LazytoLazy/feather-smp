package com.feathersmp.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds feather + upgrade token items, handles per-ability cooldowns,
 * applies each feather's weak/strong ability, and registers the crafting
 * recipes used to make and use Upgrade Tokens.
 */
public class FeatherManager {

    private final FeatherSMP plugin;
    private final NamespacedKey typeKey;
    private final NamespacedKey tierKey;
    private final NamespacedKey tokenKey;

    // playerId -> ("typeId_ABILITY" -> system time in millis when usable again)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public FeatherManager(FeatherSMP plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "feather_type");
        this.tierKey = new NamespacedKey(plugin, "feather_tier");
        this.tokenKey = new NamespacedKey(plugin, "upgrade_token");
    }

    // ----------------------------------------------------------------
    // Item building
    // ----------------------------------------------------------------

    public ItemStack createFeather(FeatherType type, FeatherTier tier) {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();

        String suffix = tier == FeatherTier.UPGRADED ? " &7(Upgraded)" : "";
        meta.setDisplayName(color(type.getDisplayName() + suffix));

        List<String> lore = new ArrayList<>();
        lore.add(color("&a&l> &aRight-Click: &f" + type.getAbilityName(AbilityTier.WEAK)));
        lore.add(color("  &7" + type.getAbilityDescription(AbilityTier.WEAK)));
        lore.add("");
        if (tier == FeatherTier.UPGRADED) {
            lore.add(color("&c&l> &cShift + Right-Click: &f" + type.getAbilityName(AbilityTier.STRONG)));
            lore.add(color("  &7" + type.getAbilityDescription(AbilityTier.STRONG)));
        } else {
            lore.add(color("&8&l> &8Shift + Right-Click: &8LOCKED"));
            lore.add(color("  &7Craft an Upgrade Token to unlock"));
        }
        lore.add("");
        lore.add(color("&8Right-click / shift-right-click to use"));
        meta.setLore(lore);

        int modelData = type.getCustomModelData() + (tier == FeatherTier.UPGRADED ? 500 : 0);
        meta.setCustomModelData(modelData);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.getId());
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.STRING, tier.name());

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createUpgradeToken() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(color("&dFeather Upgrade Token"));
        meta.setLore(List.of(
                color("&7Combine with a feather in a crafting"),
                color("&7table to permanently unlock its"),
                color("&7&oshift + right-click&r&7 ability.")
        ));
        meta.setCustomModelData(2000);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.STRING, "true");

        item.setItemMeta(meta);
        return item;
    }

    // ----------------------------------------------------------------
    // Item identification
    // ----------------------------------------------------------------

    public FeatherType getFeatherType(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (id == null) {
            return null;
        }
        return FeatherType.fromId(id);
    }

    public FeatherTier getFeatherTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return FeatherTier.BASIC;
        }
        String tier = item.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.STRING);
        if (tier == null) {
            return FeatherTier.BASIC;
        }
        try {
            return FeatherTier.valueOf(tier);
        } catch (IllegalArgumentException ex) {
            return FeatherTier.BASIC;
        }
    }

    public boolean isUpgradeToken(ItemStack item) {
        if (item == null || item.getType() != Material.AMETHYST_SHARD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(tokenKey, PersistentDataType.STRING);
    }

    // ----------------------------------------------------------------
    // Cooldowns
    // ----------------------------------------------------------------

    public boolean isOnCooldown(Player player, FeatherType type, AbilityTier ability) {
        return getRemainingCooldown(player, type, ability) > 0;
    }

    /** Remaining cooldown in whole seconds (0 if ready). */
    public long getRemainingCooldown(Player player, FeatherType type, AbilityTier ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }
        Long readyAt = playerCooldowns.get(cooldownKey(type, ability));
        if (readyAt == null) {
            return 0;
        }
        long remainingMillis = readyAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return 0;
        }
        return (remainingMillis / 1000) + 1;
    }

    private void startCooldown(Player player, FeatherType type, AbilityTier ability) {
        String path = "cooldowns." + type.getId() + "." + (ability == AbilityTier.WEAK ? "weak" : "strong");
        int seconds = plugin.getConfig().getInt(path, ability == AbilityTier.WEAK ? 20 : 90);
        if (seconds <= 0) {
            return;
        }
        cooldowns
                .computeIfAbsent(player.getUniqueId(), id -> new HashMap<>())
                .put(cooldownKey(type, ability), System.currentTimeMillis() + (seconds * 1000L));
    }

    private String cooldownKey(FeatherType type, AbilityTier ability) {
        return type.getId() + "_" + ability.name();
    }

    // ----------------------------------------------------------------
    // Action bar
    // ----------------------------------------------------------------

    /** Builds the legacy-color-coded action bar text for a held feather, or null if the item isn't a feather. */
    public String buildActionBar(Player player, ItemStack item) {
        FeatherType type = getFeatherType(item);
        if (type == null) {
            return null;
        }
        FeatherTier tier = getFeatherTier(item);

        String weakStatus = statusText(getRemainingCooldown(player, type, AbilityTier.WEAK));

        String strongStatus;
        if (tier == FeatherTier.BASIC) {
            strongStatus = "&8Locked";
        } else {
            strongStatus = statusText(getRemainingCooldown(player, type, AbilityTier.STRONG));
        }

        return color(type.getDisplayName()
                + " &8| &f" + type.getAbilityName(AbilityTier.WEAK) + ": " + weakStatus
                + " &8| &f" + type.getAbilityName(AbilityTier.STRONG) + ": " + strongStatus);
    }

    private String statusText(long remainingSeconds) {
        return remainingSeconds > 0 ? "&c" + remainingSeconds + "s" : "&aReady";
    }

    // ----------------------------------------------------------------
    // Abilities
    // ----------------------------------------------------------------

    /** Applies the ability for the given feather type + tier. Assumes the caller already checked cooldown/unlock state. */
    public void useAbility(Player player, FeatherType type, AbilityTier ability) {
        switch (type) {
            case INFERNO -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 15, 0, true, true));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60, 0, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1, true, true));
                }
            }
            case GALE -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 8, 0, true, true));
                } else {
                    Vector velocity = player.getLocation().getDirection().setY(0.9).normalize().multiply(1.4);
                    velocity.setY(1.1);
                    player.setVelocity(velocity);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 10, 0, true, true));
                }
            }
            case PHOENIX -> {
                double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                if (ability == AbilityTier.WEAK) {
                    player.setHealth(Math.min(maxHealth, player.getHealth() + 12.0));
                } else {
                    player.setHealth(maxHealth);
                    player.setFireTicks(0);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 10, 0, true, true));
                }
            }
            case SHADOW -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 15, 0, true, true));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0, true, true));
                }
            }
            case STORM -> {
                if (ability == AbilityTier.WEAK) {
                    for (LivingEntity nearby : player.getLocation().getNearbyLivingEntities(4)) {
                        if (!nearby.equals(player)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 5, 0, true, true));
                        }
                    }
                } else {
                    List<org.bukkit.block.Block> ray = player.getLineOfSight(null, 20);
                    Location strikeLocation = ray.isEmpty()
                            ? player.getLocation()
                            : ray.get(ray.size() - 1).getLocation();
                    World world = player.getWorld();
                    world.strikeLightning(strikeLocation);
                    for (LivingEntity nearby : strikeLocation.getNearbyLivingEntities(3)) {
                        if (!nearby.equals(player)) {
                            nearby.damage(6.0, player);
                        }
                    }
                }
            }
            case FROST -> {
                if (ability == AbilityTier.WEAK) {
                    for (LivingEntity nearby : player.getLocation().getNearbyLivingEntities(5)) {
                        if (!nearby.equals(player)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 3, 0, true, true));
                        }
                    }
                } else {
                    for (LivingEntity nearby : player.getLocation().getNearbyLivingEntities(5)) {
                        if (!nearby.equals(player)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 5, 2, true, true));
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 5, 1, true, true));
                        }
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 8, 1, true, true));
                }
            }
            case VOID -> {
                double distance = ability == AbilityTier.WEAK ? 3 : 8;
                Location destination = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(distance));
                destination.setY(Math.min(destination.getY(), player.getWorld().getMaxHeight() - 2));
                if (isSafeDestination(destination)) {
                    player.teleport(destination);
                } else {
                    player.teleport(player.getLocation().add(player.getLocation().getDirection().normalize().multiply(1)));
                }
            }
            case AQUA -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 20 * 60, 0, true, true));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 20 * 180, 0, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20 * 180, 0, true, true));
                }
            }
            case STONE -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 10, 0, true, true));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 15, 3, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 15, 1, true, true));
                }
            }
            case RADIANT -> {
                if (ability == AbilityTier.WEAK) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 30, 0, true, true));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, true, true));
                    for (LivingEntity nearby : player.getLocation().getNearbyLivingEntities(15)) {
                        if (!nearby.equals(player)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 0, true, true));
                        }
                    }
                }
            }
        }

        startCooldown(player, type, ability);
    }

    private boolean isSafeDestination(Location location) {
        Material feet = location.getBlock().getType();
        Material head = location.clone().add(0, 1, 0).getBlock().getType();
        return !feet.isSolid() && !head.isSolid();
    }

    // ----------------------------------------------------------------
    // Recipes
    // ----------------------------------------------------------------

    /** Registers the Upgrade Token recipe and the 10 "upgrade a feather" recipes. Call once from onEnable. */
    public void registerRecipes() {
        ShapedRecipe tokenRecipe = new ShapedRecipe(new NamespacedKey(plugin, "upgrade_token"), createUpgradeToken());
        tokenRecipe.shape("PGP", "GNG", "PGP");
        tokenRecipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        tokenRecipe.setIngredient('G', Material.GHAST_TEAR);
        tokenRecipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe(tokenRecipe);

        for (FeatherType type : FeatherType.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + type.getId());
            ItemStack result = createFeather(type, FeatherTier.UPGRADED);
            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            recipe.addIngredient(new RecipeChoice.ExactChoice(createFeather(type, FeatherTier.BASIC)));
            recipe.addIngredient(new RecipeChoice.ExactChoice(createUpgradeToken()));
            Bukkit.addRecipe(recipe);
        }
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
