
# the-great-chicken / TGC-Plugin

The Great Chicken plugin for SGP

## Build and releases

Build with Java 25 and `mvn clean verify`; the JAR is written to `target/`. Change the version in `pom.xml` using the format `26.1.2-1.1` and push to `main` to publish a GitHub release with the JAR attached. Maven fills in `plugin.yml` automatically.

## Website cosmetics

Requires Paper, DiscordSRV and PacketEvents. In `config.yml`, enable `cosmetics.enabled` and set `cosmetics.secret` to a random base64url secret of at least 32 characters. Set the same secret in the website's `COSMETICS_BRIDGE_SECRET` and point `COSMETICS_BRIDGE_URL` at the private listener (default `http://127.0.0.1:8766`). Restart to apply configuration changes.

`cosmetics.datapack` names the enabled directory under the primary world's `datapacks` folder (default `TGCdatapack`). Cosmetics are read from its declarations and equip hooks on startup and datapack reload. Players must be online to change equipment.

## Glowing
This part of the plugin provides player-specific glowing.
It allows entities to appear glowing only for specific players, and optionally with a specific color, without modifying player teams.

### Commands
- `/glow` : opens the glow management interface.
- `/glow add <players> <entities> [<color>]` : makes the specified entities glow permanently for the given players.
An optional color can be provided
- `/glow time <players> <entities> <duration> [<color>]` : makes the specified entities glow for a given duration for the given players.
An optional color can be provided
- `/glow remove <players> <entities>` : removes the glow effect from specified entities for the specified players.
- `/useglow` : makes nearby players glow for a configurable minimum distance and duration (if the command is enabled in the config).
- `/useglow toggle` : enables or disables the use of the `useglow` command.

### Config:
```yml
glow: #all the parameter of the useglow and glow command
  time: 100 #the duration of the useglow effect in ticks
  cooldown: 200 #the time to wait before reusing the useglow command in ticks
  minDistance: 8 #the minimum distance a player must be from to be glowed
  color: WHITE #the default color to use when not specified
  timeText: "You need to wait %ds" #the message send when useglow is reloading (%d is the time remaining)
  disabledMessage: "Glow use not enabled" # the message to send when useglow is disabled
```
