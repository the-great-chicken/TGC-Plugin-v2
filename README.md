
# the-great-chicken / TGC-Plugin

The Great Chicken plugin for SGP

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
```