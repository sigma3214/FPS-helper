# FPS HELPER — Minecraft 1.21.4 Fabric

Client-side Fabric mod that places configurable arrows around the screen pointing toward nearby players and mobs.

## Controls
- **G** — toggle arrows on/off.

## Configuration
After first launch, edit:
`config/fpshelper.json`

Important options:
- `showPlayers` — players
- `showMobs` — mobs
- `showHostile` — hostile mobs
- `showPassive` — passive/other mobs
- `maxDistance` — maximum distance in blocks
- `radius` — distance of arrows from screen center
- `arrowSize` — arrow size
- `arrow` — arrow character, e.g. `▲`, `◆`, `➤`, `●`
- `arrowColor` — ARGB color, e.g. `4294967295` for white
- `outline` — text outline
- `showDistance` — distance under arrow
- `hideWhenOnScreen` — don't show arrows for targets in the front 90° cone
- `showInvisible` — include invisible entities

The mod is client-side. It does not add entities or change the server.

Build with Java 21 and Gradle/Loom. Versions are set for Minecraft 1.21.4, Yarn 1.21.4+build.8, Fabric Loader 0.16.9 and Fabric API 0.110.5+1.21.4.
