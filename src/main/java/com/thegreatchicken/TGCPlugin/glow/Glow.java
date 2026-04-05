package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.thegreatchicken.TGCPlugin.PluginLoader;
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

import java.util.*;

public class Glow {

    private static final HashMap<Integer,Glow> glowMap = new HashMap<>();
    private final HashMap<UUID, GlowInstance> players;
    private final Entity glowEntity;
    
    private Team tempTeam;


    private Glow(Entity entity, HashMap<Player, Pair<NamedTextColor,Long>> players) {
        this.glowEntity = entity;
        HashMap<UUID, Pair<Team, Integer>> Teams = new HashMap<>();
        for (Map.Entry<Player, Pair<NamedTextColor, Long>> entry : players.entrySet()) {
            NamedTextColor color = entry.getValue().getKey();
            Team team = color == null ? null : createTeam(entry.getValue().getKey());
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

    private Team createTeam(NamedTextColor color){
        if (color == null ) throw new IllegalArgumentException("NamedTextColor must be a color " +
                "format");
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(color.asHexString());
        if (team == null){
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(color.asHexString());
        }
        team.color(color);
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
                    sendTeamPacket(player, createTeam(color), true);
                ChangeColor(player, color);
                return;
            }
        }
        pair.setA(createTeam(NamedTextColor));
        sendTeamPacket(player,pair.getKey(),true);
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
        tempTeam = players.get(ID).getKey();
        players.remove(ID);
        removeGlow(player);
        if (players.isEmpty())
            glowMap.remove(glowEntity.getEntityId());

    }

    public void removeGlow(){
        glowMap.remove(glowEntity.getEntityId());
        for (UUID player: players.keySet()){
            tempTeam = players.get(player).getKey();
            Player player1 = Bukkit.getPlayer(player);
            assert player1 != null;
            removeGlow(player1);
        }
    }
    
    public static void removeGlow(Entity entity){
        int uid = entity.getEntityId();
        if (glowMap.containsKey(uid)){
            glowMap.get(uid).removeGlow();
        }
    }

    private void addGlow(Player player,Team team){
        sendGlowPacket(player,true,glowEntity.getEntityId());
        if (team == null) return;
        sendTeamPacket(player,team,true);
        sendTeamPacket(player,team, getEntityId(glowEntity), ClientboundSetTeamPacket.Action.ADD);
    }

    private void removeGlow(Player player){
        if (!player.hasPotionEffect(PotionEffectType.GLOWING))
            sendGlowPacket(player,false,glowEntity.getEntityId());
        if (tempTeam == null) return;
        removeTeam(player);
        tempTeam = null;
    }

    private void ChangeColor(Player player,NamedTextColor color){
        UUID ID = player.getUniqueId();
        Pair<Team,Integer> Pair = players.get(ID);
        Team team = players.get(ID).getKey();
        team.setColor(color);
        sendTeamPacket(player,team,false);
        Pair.setA(team);
        players.put(ID,Pair);
    }

    private void removeTeam(Player client){
        if (tempTeam == null) return;
        String uid = getEntityId(glowEntity);
        sendTeamPacket(client,tempTeam, uid, ClientboundSetTeamPacket.Action.REMOVE);
        sendTeamRemovePacket(client,tempTeam);

        Team team =((CraftScoreboard) client.getScoreboard()).getHandle().getPlayersTeam(uid);
        if (team != null)
            sendTeamPacket(client,team, uid, ClientboundSetTeamPacket.Action.ADD);

    }


    //====================Packet====================

    private static void sendGlowPacket(Player player,boolean glowing,int id){
        player.sendMessage("the entity "+id+" is "+(glowing ? "glowing" : "not glowing"));
        byte glowingByte = glowing ? 0x40 : (byte) 0;
        List<EntityData<?>> entityData = List.of(new EntityData<>(0, EntityDataTypes.BYTE, glowingByte));
        var packet = new WrapperPlayServerEntityMetadata(id,entityData);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private static void sendTeamPacket(Player player, Team team,boolean create) {
        player.sendMessage("the team "+team.getName()+" is "+(create ? "created" : "modified"));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,createAddOrModifyPacket(team, create));
    }

    private static void sendTeamRemovePacket(Player player, Team team) {
        player.sendMessage("the team "+team.getName()+" is removed");
        getConnection(player).send(createRemovePacket(team));
    }

    private static void sendTeamPacket(Player player1, Team team, String entity,
                                        ClientboundSetTeamPacket.Action action) {
        player1.sendMessage("the entity "+entity+" is "+(action == Action.ADD ? "added" : "removed")+" to the team "+team.getName());
        getConnection(player1).send(createPlayerPacket(team,entity,action));
    }

    private static String getEntityId(Entity entity){
        return entity instanceof Player player ? player.getName() : entity.getUniqueId().toString();
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

    //====================Packet Listener====================
    
    public static void registerGlowListener(ProtocolManager protocolManager){
        protocolManager.addPacketListener(new PacketAdapter(
                PluginLoader.PLUGIN,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.SCOREBOARD_TEAM
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                ClientboundSetTeamPacket GlowPlayersNames = (ClientboundSetTeamPacket) event.getPacket().getHandle();
                String[] players = GlowPlayersNames.getPlayers().toArray(new String[0]);
                for (String player: players){
                    Player player1 = Bukkit.getPlayer(player);
                    int id = player1 == null? -1 : player1.getEntityId();
                    if (!glowMap.containsKey(id)) continue;
                    Glow glow = glowMap.get(id);
                    if (!glow.players.containsKey(event.getPlayer().getUniqueId()) ||
                            glow.players.get(event.getPlayer().getUniqueId()).getKey() == null) continue;
                    event.getPlayer().sendMessage("team packet canceled");
                    event.setCancelled(true);
                }

            }
        });

        protocolManager.addPacketListener(new PacketAdapter(
                PluginLoader.PLUGIN,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_METADATA
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                ClientboundSetEntityDataPacket GlowPacket =
                        (ClientboundSetEntityDataPacket) event.getPacket().getHandle();
                List<SynchedEntityData.DataValue<?>> edata = GlowPacket.packedItems();
                if(edata.contains(SynchedEntityData.DataValue.create(
                        new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) 0x40))
                        || edata.contains(SynchedEntityData.DataValue.create(
                                new EntityDataAccessor<>(0,EntityDataSerializers.BYTE), (byte) 0))){
                    Glow glow = getGlowByEntityID(GlowPacket.id());
                    if (glow == null || !glow.players.containsKey(event.getPlayer().getUniqueId())) return;
                    event.getPlayer().sendMessage("glow packet canceled");
                    event.setCancelled(true);
                }
            }
        });

        PluginLoader.PLUGIN.getLogger().info("packet listener load");
    }

    public static void loadGlow(Player player){
        player.sendMessage("load glow");
        getGlowEntitys().forEach(entity -> {
            Glow glow = glowMap.get(entity);
            if (glow.players.containsKey(player.getUniqueId())){
                Pair<Team,Integer> temp = glow.players.remove(player.getUniqueId());
                Team team = temp.getKey();
                sendGlowPacket(player,true,entity);
                sendTeamPacket(player,team,true);
                sendTeamPacket(player,team,getEntityId(glow.glowEntity), ClientboundSetTeamPacket.Action.ADD);
                glow.players.put(player.getUniqueId(),temp);
            }
        });
    }



    //====================Getter====================

    public static Glow getGlowByEntityID(int id){
        for (Glow glow: glowMap.values()){
            if (glow.glowEntity.getEntityId() == id) return glow;
        }
        return null;
    }



    public static Set<Integer> getGlowEntitys(){
        return glowMap.keySet();
    }
}
