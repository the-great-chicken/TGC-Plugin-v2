package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.thegreatchicken.TGCPlugin.PluginLoader;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import static com.thegreatchicken.TGCPlugin.glow.PacketUtils.*;

import java.util.*;

public class Glow {

    private static final HashMap<Integer,Glow> glowMap = new HashMap<>();
    private final HashMap<UUID, GlowInstance> players;
    @Getter
    private final Entity glowEntity;

    private Glow(Entity entity, HashMap<Player, Pair<NamedTextColor,Long>> players) {
        this.glowEntity = entity;
        HashMap<UUID, GlowInstance> Teams = new HashMap<>();
        for (Map.Entry<Player, Pair<NamedTextColor, Long>> entry : players.entrySet()) {
            NamedTextColor color = entry.getValue().getKey();
            var team = color == null ? null : createTeam(entry.getValue().getKey());
            Long time = entry.getValue().getValue();
            addGlow(entry.getKey(), team);
            int id = -1;
            if (time != -1) {
                id = scheduler(entry.getKey(), time);
            }
            Teams.put(entry.getKey().getUniqueId(), new MutablePair<>(team, id));

        }
        this.players = Teams;

    }

    public static Glow setGlow(Entity entity){
        return setGlow(entity,new HashMap<>());
    }

    public static Glow setGlow(Entity entity, HashMap<Player,NamedTextColor> players){
        HashMap<Player, Pair<NamedTextColor,Long>> teams = new HashMap<>();
        for (Map.Entry<Player,NamedTextColor> player: players.entrySet()){
            teams.put(player.getKey(),new MutablePair<>(player.getValue(),-1L));
        }
        return setGlowTime(entity,teams);
    }

    @SuppressWarnings("DataFlowIssue")
    public static Glow setGlowTime(Entity entity, HashMap<Player,Pair<NamedTextColor,Long>> players){
        Integer uid = entity.getEntityId();
        if (glowMap.containsKey(uid)){
            Glow glow = glowMap.get(uid);
            for (Map.Entry<Player,Pair<NamedTextColor,Long>> entry: players.entrySet()){
                NamedTextColor color = entry.getValue().getKey();
                System.out.println(entry.getValue().getKey()+" : "+ color.asHexString());
                glow.addPlayerTime(entry.getKey(),color, entry.getValue().getValue());
            }
            return glowMap.put(uid,glow);
        }
        Glow glow = new Glow(entity,players);
        glowMap.put(uid,glow);
        return glow;
    }

    private WrapperPlayServerTeams.ScoreBoardTeamInfo createTeam(NamedTextColor color){
        if (color == null ) throw new IllegalArgumentException("NamedTextColor must be a color " +
                "format");
        var team = new WrapperPlayServerTeams.ScoreBoardTeamInfo(Component.empty(),null,null,
            WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
            WrapperPlayServerTeams.CollisionRule.ALWAYS, color,
            WrapperPlayServerTeams.OptionData.ALL);
        return team;
    }

    public void addPlayer(Player player, NamedTextColor color) {
        addPlayerTime(player,color,-1L);
    }

    public void addPlayerTime(Player player, NamedTextColor color, Long time) {
        if (color == null)
            throw new IllegalArgumentException("NamedTextColor must be a color format" + color);

        UUID id = player.getUniqueId();
        Pair<Team,Integer> pair = players.computeIfAbsent(id,k -> new MutablePair<>(null,scheduler(player, time)));


        //si le joueur n'est pas dans la liste
        if (!players.containsKey(id)){
            sendGlowPacket(player,true,glowEntity.getEntityId());
            if (time != -1) {
                pair.setValue(scheduler(player, time));
            }
        }
        else {
            //si le joueur est dans la liste mais que le temps n'est pas null
            if (pair.getValue() != -1) {
                Bukkit.getScheduler().cancelTask(pair.getValue());
                pair.setValue(scheduler(player, time));
            }
            //si le joueur est dans la liste mais que la couleur est différente
            if (pair.getKey().color() != color) {
                if (players.containsKey(id) && players.get(id).getKey() == null)
                    sendTeamCreatePacket(player, createTeam(color), true);
                ChangeColor(player, color);
                return;
            }
        }
        pair.setA(createTeam(NamedTextColor));
        sendTeamCreatePacket(player,pair.getKey(),true);
        players.put(id, pair);

    }

    public void addPlayers(HashMap<Player,NamedTextColor> players){
        for (Map.Entry<Player,NamedTextColor> entry: players.entrySet()){
            addPlayer(entry.getKey(),entry.getValue());
        }
    }

    public void addPlayersTime(HashMap<Player,Pair<NamedTextColor,Long>> players){
        for (Map.Entry<Player,Pair<NamedTextColor,Long>> entry: players.entrySet()){
            addPlayerTime(entry.getKey(),entry.getValue().getKey(),entry.getValue().getValue());
        }
    }

    public void removePlayer(Player player){
        UUID ID = player.getUniqueId();
        if (!players.containsKey(ID)) return;
        removeGlow(player);
        players.remove(ID);
        if (players.isEmpty())
            glowMap.remove(glowEntity.getEntityId());

    }

    public void removeGlow(){
        for (UUID player: players.keySet()){
            Player player1 = Bukkit.getPlayer(player);
            assert player1 != null;
            removeGlow(player1);
        }
        glowMap.remove(glowEntity.getEntityId());
    }
    
    public static void removeGlow(Entity entity){
        int uid = entity.getEntityId();
        if (glowMap.containsKey(uid)){
            glowMap.get(uid).removeGlow();
        }
    }

    private void addGlow(Player player, GlowInstance team){
        sendGlowPacket(player,true,glowEntity.getEntityId());
        if (team == null) return;
        sendTeamCreatePacket(player,team,true);
        sendTeamJoinLeavePacket(player,team, getEntityId(glowEntity), true);
    }

    private void removeGlow(Player player){
        if (!player.hasPotionEffect(PotionEffectType.GLOWING))
            sendGlowPacket(player,false,glowEntity.getEntityId());
        removeTeam(player);
    }

    private void ChangeColor(Player player,NamedTextColor color){
        UUID ID = player.getUniqueId();
        GlowInstance instance = players.get(ID);
        instance.color(color);
        sendTeamCreatePacket(player,instance,false);
    }

    private void removeTeam(Player client){
        var tempTeam = players.get(client.getUniqueId());
        String uid = getEntityId(glowEntity);
        sendTeamJoinLeavePacket(client,tempTeam, uid, false);
        sendTeamRemovePacket(client,tempTeam);

        Team team =client.getScoreboard().getTeam(uid);
        if (team != null)
            sendTeamJoinLeavePacket(client,team, uid, true);
    }


    private int scheduler(Player player, long time){
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                removePlayer(player);
            }
        }.runTaskLater(PluginLoader.PLUGIN,time);
        return task.getTaskId();
    }

    public static Glow getGlowByEntityID(int id){
        for (Glow glow: glowMap.values()){
            if (glow.glowEntity.getEntityId() == id) return glow;
        }
        return null;
    }

    public static Set<Integer> getGlowEntities(){
        return glowMap.keySet();
    }

    public static boolean hasGlow(int id){
        return glowMap.containsKey(id);
    }

    public static Glow getGlow(Integer id){
        return glowMap.get(id);
    }

    public boolean hasGlow(UUID player){
        return players.containsKey(player);
    }

    public GlowInstance getGlow(UUID player){
        return players.get(player);
    }
}
