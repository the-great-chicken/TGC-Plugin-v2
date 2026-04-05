package com.thegreatchicken.TGCPlugin.glow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.scoreboard.Team;

@AllArgsConstructor
@Getter
@Setter
@Accessors(fluent = true,chain = false)
public class GlowInstance {

    private Team team;
    private int schedulerId;
}
