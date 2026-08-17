# FeatherSMP

A custom-ability item plugin for **Feather SMP**, in the spirit of Bliss SMP's
gems — but built around 10 unique **feathers** instead.

Target: **Paper 1.21.11**, Java 21.

## How it works

Every feather has **two abilities**:

- **Weak ability** — triggered by a plain **right-click**. Always usable.
- **Strong ability** — triggered by **shift + right-click**. Locked until the
  feather is upgraded.

To unlock the strong ability, craft a **Feather Upgrade Token**, then combine
it with the feather in a crafting table (shapeless: 1 feather + 1 token →
the same feather, upgraded). The upgrade is permanent for that item.

While a player is holding a feather, an **action bar** message shows both
ability names and their live status — a green "Ready", a red countdown while
on cooldown, or "Locked" for a strong ability that hasn't been unlocked yet.

Feathers are **reusable** (gated by cooldowns, not consumed) by default —
toggle `consume-on-use: true` in `config.yml` if you'd rather they be
single-use instead.

## The 10 Feathers

| Feather | Weak (right-click) | Strong (shift + right-click, needs token) |
|---|---|---|
| Feather of Inferno | Fire Resistance (15s) | Fire Resistance (60s) + Speed II (10s) |
| Feather of the Gale | Slow Falling (8s) | Launches you skyward + Slow Falling (10s) |
| Feather of the Phoenix | Heals 6 hearts | Full heal + Regen II + Fire Resistance |
| Feather of Shadow | Speed I (15s) | Invisibility + Speed (30s) |
| Feather of the Storm | Weakens nearby enemies briefly | Lightning strike where you're looking |
| Feather of Frost | Slows nearby enemies briefly | Heavily slows enemies + speeds you up |
| Feather of the Void | Blinks you forward ~3 blocks | Blinks you forward ~8 blocks |
| Feather of the Deep | Water Breathing (60s) | Water Breathing + Dolphin's Grace (3 min) |
| Feather of Stone | Resistance I (10s) | Resistance IV + Absorption (15s) |
| Feather of the Radiant | Night Vision (30s) | Night Vision (60s) + reveals nearby enemies |

All cooldowns (set separately per weak/strong ability) and every message are
configurable in `config.yml`.

## Getting an Upgrade Token

Craft it in a crafting table (shaped 3x3):

```
Phantom Membrane | Ghast Tear | Phantom Membrane
Ghast Tear        | Nether Star | Ghast Tear
Phantom Membrane | Ghast Tear | Phantom Membrane
```

Then combine the token with a basic feather (any arrangement in the grid,
it's a shapeless recipe) to get the upgraded version of that same feather.

## Building

```bash
mvn clean package
```

The compiled jar will be in `target/FeatherSMP-1.0.0.jar`. Drop it into your
server's `plugins/` folder and restart.

> **Note on the Paper API version:** this project targets
> `1.21.11-R0.1-SNAPSHOT`. If your server is running a different Paper
> version, update the `<version>` in `pom.xml` to match — Paper API versions
> are tied to a specific Minecraft version.

## Commands

- `/feather list` — see all 10 feather types and both abilities for each
  (`feathersmp.use`, default: everyone).
- `/feather give <player> <type> [basic|upgraded] [amount]` — give a feather,
  optionally already upgraded (`feathersmp.give`, default: op).
- `/feather items <player> [amount]` — give Feather Upgrade Tokens directly,
  bypassing the crafting recipe (`feathersmp.items`, default: op).
- `/feather reload` — reload `config.yml` (`feathersmp.reload`, default: op).

`feathersmp.admin` (default: op) grants all of the above.

## Customization

- Every feather is tagged with persistent data (`feather_type` +
  `feather_tier`) rather than relying on its display name, so renaming an
  item in an anvil won't break it.
- Each feather also has a unique `CustomModelData` value (base ids
  1001–1010, +500 once upgraded) so a resource pack can give the basic and
  upgraded versions of each feather their own texture.
- Ability logic lives in `FeatherManager#useAbility` — add a new `case` there
  and a new enum constant in `FeatherType` to add an 11th feather.
