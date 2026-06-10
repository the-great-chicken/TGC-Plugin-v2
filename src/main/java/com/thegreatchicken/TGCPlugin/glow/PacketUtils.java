package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;

public final class PacketUtils {

    public static void sendGlowPacket(Player player, boolean glowing, int id){
        byte glowingByte = glowing ? 0x40 : (byte) 0;
        List<EntityData<?>> entityData = List.of(new EntityData<>(0, EntityDataTypes.BYTE, glowingByte));
        var packet = new WrapperPlayServerEntityMetadata(id,entityData);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
    }

    public static void sendTeamCreatePacket(Player player, GlowInstance team, boolean create) {
        var mode = create ? WrapperPlayServerTeams.TeamMode.CREATE : WrapperPlayServerTeams.TeamMode.UPDATE;
        WrapperPlayServerTeams teamPacket = new WrapperPlayServerTeams(team.getTeamName(), mode,team.team());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player,teamPacket);
    }

    public static void sendTeamRemovePacket(Player player, GlowInstance team) {
        var mode = WrapperPlayServerTeams.TeamMode.REMOVE;
        WrapperPlayServerTeams teamPacket = new WrapperPlayServerTeams(team.getTeamName(), mode,team.team());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player,teamPacket);
    }

    public static void sendTeamJoinLeavePacket(Player client, GlowInstance team, String entity, boolean add) {
        var mode = add ? WrapperPlayServerTeams.TeamMode.ADD_ENTITIES : WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES;
        WrapperPlayServerTeams teamPacket = new WrapperPlayServerTeams(team.getTeamName(), mode,team.team(),entity);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(client,teamPacket);
    }

    public static void sendTeamJoinLeavePacket(Player client, Team team, String entity, boolean add) {
        var instance = new GlowInstance(getScoreboardTeamInfo(team),-1);
        sendTeamJoinLeavePacket(client,instance,entity,add);
    }

    public static WrapperPlayServerTeams.ScoreBoardTeamInfo getScoreboardTeamInfo(Team team){
        var visibility = getNameTagVisibility(team.getOption(Team.Option.NAME_TAG_VISIBILITY));
        var collision = getCollisionRule(team.getOption(Team.Option.COLLISION_RULE));
        return new WrapperPlayServerTeams.ScoreBoardTeamInfo(
            team.displayName(),team.prefix(),team.suffix(),visibility,collision, NamedTextColor.nearestTo(team.color()),getOptionData(team)
        );
    }

    public static WrapperPlayServerTeams.NameTagVisibility getNameTagVisibility(Team.OptionStatus option){
        return switch (option){
            case ALWAYS -> WrapperPlayServerTeams.NameTagVisibility.ALWAYS;
            case NEVER -> WrapperPlayServerTeams.NameTagVisibility.NEVER;
            case FOR_OTHER_TEAMS -> WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            case FOR_OWN_TEAM -> WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OWN_TEAM;
        };
    }

    public static WrapperPlayServerTeams.CollisionRule getCollisionRule(Team.OptionStatus option){
        return switch (option){
            case ALWAYS -> WrapperPlayServerTeams.CollisionRule.ALWAYS;
            case NEVER -> WrapperPlayServerTeams.CollisionRule.NEVER;
            case FOR_OTHER_TEAMS -> WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS;
            case FOR_OWN_TEAM -> WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM;
        };
    }

    public static WrapperPlayServerTeams.OptionData getOptionData(Team team){
        if (team.canSeeFriendlyInvisibles()) {
            if (team.allowFriendlyFire())
                return WrapperPlayServerTeams.OptionData.ALL;
            return WrapperPlayServerTeams.OptionData.FRIENDLY_CAN_SEE_INVISIBLE;
        }
        if (team.allowFriendlyFire())
            return WrapperPlayServerTeams.OptionData.FRIENDLY_FIRE;
        return WrapperPlayServerTeams.OptionData.NONE;
    }


    public static String getEntityId(Entity entity){
        return entity instanceof Player player ? player.getName() : entity.getUniqueId().toString();
    }
}
