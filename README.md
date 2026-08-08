# Valence

Modular vanilla+ additions for Minecraft on Fabric. Every feature is an independent
module that can be toggled in the config; with everything off the game is vanilla.

- Minecraft 26.1.x and 26.2, Fabric
- Requires [Fabric API](https://modrinth.com/mod/fabric-api)
- License: MIT

## Modules

**Building**

- **Vertical Slabs** — a vertical counterpart for every vanilla slab material. Placement
  picks the half from where you click; two halves merge back into the full block.
- **Woodworks** — ladders, bookshelves, posts and chests for every wood family.

**Utility**

- **Animal Trough** — a hopper-fillable block that feeds breedable animals in range.
  Trough-bred animals drop no experience, and it idles once the area is crowded.
- **Seed Satchel** — a bag that gathers plantable items as you pick them up and sows
  them straight from its contents. Sneak-use to open it.

**Interface**

- **Inventory Actions** — sort, deposit-matching, deposit-everything and
  extract-matching buttons on container screens, plus a sort keybind. Works on servers
  without the mod; sorts in one packet on servers that have it.
- **Info HUD** — an overlay with coordinates, facing, biome, light, FPS and time,
  toggled with F6. Each line can be turned off.
- **Held Item Readout** — shows durability and count beside the hotbar when they change.

**Placement**

- **Reacharound** — look past a ledge edge and place onto the hidden face, bridging
  forward or downward without sneaking. Singleplayer, or multiplayer when the server
  runs Valence and allows it.

## Settings

Open the settings screen from Mod Menu, the title screen, the pause menu, or a keybind.
Everything is also editable in `config/valence.json5`, which keeps your comments and any
keys it does not recognise. Recipe and creative-tab changes take effect after `/reload`.

Modules that duplicate a mod you already have default to off on first run — Reacharound
with Tweakeroo, Inventory Actions with Inventory Profiles Next, MouseWheelie or
Inventory Sorter. Your own setting is never overridden.

## Building

```
./gradlew build
```

Builds every supported Minecraft version. Jars land in `versions/<mc>/build/libs/`.
