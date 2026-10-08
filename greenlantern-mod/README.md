# Green Lantern Mod (Fabric, Minecraft 1.21.1)

An unofficial fan mod. All textures are original and generated; no DC assets are used.

## Features
- **Power Ring** – 4 modes, **Sneak + Right-click** cycles, **Right-click** uses:
  - Energy Blast: 48-block hitscan beam, 10 damage, knockback, glowing
  - Construct: Bridge: 3-wide, 16-long hard-light walkway that fades
  - Construct: Fist: giant hard-light fist with forearm that launches enemies
  - Construct: Shield: hollow hard-light sphere around you
  - Flight: toggles fast flight (drains 1 charge/sec, slow-fall when power runs out)
- **Power Battery** – right-click to recharge every ring in your inventory
- Rings also regain 1 charge every 5 seconds passively
- Charge bar on the item, tooltips, particles, sounds, creative tab, recipes

## Recipes
- Power Ring: gold ingots (corners), emeralds (edges), diamond (centre)
- Power Battery: iron, glass, 2 emerald blocks, lantern

## Build
1. JDK 21 + Gradle 8.8+ (or copy `gradlew`/`gradle/` from the Fabric example mod).
2. `./gradlew build`, the jar is in `build/libs/`.
3. Drop it in `mods/` with Fabric Loader 0.16+ and Fabric API for 1.21.1.
Dev client: `./gradlew runClient`.

## No-install build (GitHub)
1. Make a free GitHub repo and upload the contents of this folder (including the hidden `.github` folder).
2. Open the repo's **Actions** tab, wait for "Build mod" to finish (~3 min).
3. Open the run, download the **greenlantern-mod-jar** artifact, unzip it, and use the jar that does NOT end in `-sources`.

## Construct UI
- **HUD panel** (left side) appears while you hold the ring: all constructs, the selected one, costs, and a charge bar.
- **Construct Wheel**: press **G** (rebindable in Controls > Green Lantern), then click a construct or press 1-5.

## The oath
Right-click a Power Battery while carrying a ring and your character recites a four-line oath in chat (nearby players hear it).
The words are in `assets/greenlantern/lang/en_us.json` under `oath.greenlantern.1` to `.4`. Edit them to write your own.
