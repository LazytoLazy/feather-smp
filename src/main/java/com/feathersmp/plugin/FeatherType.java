package com.feathersmp.plugin;

/**
 * The 10 custom feathers available on Feather SMP.
 * Each feather has a weak ability (always usable, plain right-click) and a
 * strong ability (shift + right-click, only usable once the feather has
 * been upgraded with an Upgrade Token).
 */
public enum FeatherType {

    INFERNO(
            "inferno", "&6Feather of Inferno", 1001,
            "Ember's Warmth", "Fire Resistance (15s)",
            "Inferno's Wrath", "Fire Resistance (60s) + Speed II (10s)"
    ),
    GALE(
            "gale", "&bFeather of the Gale", 1002,
            "Light Breeze", "Slow Falling (8s)",
            "Gale Force", "Launches you skyward + Slow Falling (10s)"
    ),
    PHOENIX(
            "phoenix", "&cFeather of the Phoenix", 1003,
            "Ember Mend", "Heals 6 hearts",
            "Rebirth", "Full heal + Regeneration II + Fire Resistance"
    ),
    SHADOW(
            "shadow", "&8Feather of Shadow", 1004,
            "Quickstep", "Speed I (15s)",
            "Veil of Shadow", "Invisibility + Speed (30s)"
    ),
    STORM(
            "storm", "&eFeather of the Storm", 1005,
            "Static Shock", "Weakens nearby enemies briefly",
            "Thunderstrike", "Calls down lightning where you're looking"
    ),
    FROST(
            "frost", "&bFeather of Frost", 1006,
            "Chilling Touch", "Slows nearby enemies briefly",
            "Absolute Zero", "Heavily slows enemies + speeds you up"
    ),
    VOID(
            "void", "&5Feather of the Void", 1007,
            "Short Step", "Blinks you forward ~3 blocks",
            "Void Walk", "Blinks you forward ~8 blocks"
    ),
    AQUA(
            "aqua", "&3Feather of the Deep", 1008,
            "Deep Breath", "Water Breathing (60s)",
            "Tide's Grace", "Water Breathing + Dolphin's Grace (3 min)"
    ),
    STONE(
            "stone", "&7Feather of Stone", 1009,
            "Hardened Skin", "Resistance I (10s)",
            "Fortress", "Resistance IV + Absorption (15s)"
    ),
    RADIANT(
            "radiant", "&fFeather of the Radiant", 1010,
            "Clear Sight", "Night Vision (30s)",
            "Radiant Beacon", "Night Vision (60s) + reveals nearby enemies"
    );

    private final String id;
    private final String displayName;
    private final int customModelData;
    private final String weakName;
    private final String weakDescription;
    private final String strongName;
    private final String strongDescription;

    FeatherType(String id, String displayName, int customModelData,
                String weakName, String weakDescription,
                String strongName, String strongDescription) {
        this.id = id;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.weakName = weakName;
        this.weakDescription = weakDescription;
        this.strongName = strongName;
        this.strongDescription = strongDescription;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getAbilityName(AbilityTier ability) {
        return ability == AbilityTier.WEAK ? weakName : strongName;
    }

    public String getAbilityDescription(AbilityTier ability) {
        return ability == AbilityTier.WEAK ? weakDescription : strongDescription;
    }

    /**
     * Looks up a feather type by its config id (case-insensitive).
     * Returns null if no match is found.
     */
    public static FeatherType fromId(String id) {
        for (FeatherType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
