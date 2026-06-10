package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;

@AllArgsConstructor
@Getter
@Setter
@Accessors(fluent = true,chain = false)
public class GlowInstance {

    private WrapperPlayServerTeams.ScoreBoardTeamInfo team;
    private int schedulerId;

    public String getTeamName(){
        return color().asHexString();
    }

    public void color(NamedTextColor color){
        team.setColor(color);
    }

    public NamedTextColor color(){
        return team.getColor();
    }
}
