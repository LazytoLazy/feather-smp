package com.feathersmp.plugin;

/**
 * Every feather has two abilities:
 * WEAK   - triggered by a plain right-click, always usable.
 * STRONG - triggered by shift + right-click, only usable once the
 *          feather has been upgraded with an Upgrade Token.
 */
public enum AbilityTier {
    WEAK,
    STRONG
}
