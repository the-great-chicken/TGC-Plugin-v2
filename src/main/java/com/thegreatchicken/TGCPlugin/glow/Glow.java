package com.thegreatchicken.TGCPlugin.glow;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.thegreatchicken.TGCPlugin.PluginLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.*;

public class Glow {

    private static final HashMap<Integer,Glow> glowMap = new HashMap<>();
    private final HashMap<UUID, Tuple<PlayerTeam,Integer>> players;
    private final Entity glowEntity;

    private static final Scoreboard SCOREBOARD = new Scoreboard();
    private PlayerTeam tempTeam;


    private Glow(Entity entity, HashMap<Player, Tuple<ChatFormatting,Long>> players) {
        this.glowEntity = entity;
        HashMap<UUID, Tuple<PlayerTeam, Integer>> playerTeams = new HashMap<>();
        for (Map.Entry<Player, Tuple<ChatFormatting, Long>> entry : players.entrySet()) {
            ChatFormatting color = entry.getValue().getA();
            PlayerTeam team = color == null ? null : createTeam(entry.getValue().getA());
            Long time = entry.getValue().getB();
            addGlow(entry.getKey(), team);
            int id = -1;
            if (time != -1) {
                id = scheduler(entry.getKey(), time);
            }
            playerTeams.put(entry.getKey().getUniqueId(), new Tuple<>(team, id));

        }
        this.players = playerTeams;

    }

    public static Glow setGlow(Entity entity){
        return setGlow(entity,new HashMap<>());
    }

    public static Glow setGlow(Entity entity, HashMap<Player,ChatFormatting> players){
        HashMap<Player, Tuple<ChatFormatting,Long>> playerTeams = new HashMap<>();
        for (Map.Entry<Player,ChatFormatting> player: players.entrySet()){
            playerTeams.put(player.getKey(),new Tuple<>(player.getValue(),-1L));
        }
        return setGlowTime(entity,playerTeams);
    }

    public static Glow setGlowTime(Entity entity, HashMap<Player,Tuple<ChatFormatting,Long>> players){
        Integer uid = entity.getEntityId();
        if (glowMap.containsKey(uid)){
            Glow glow = glowMap.get(uid);
            for (Map.Entry<Player,Tuple<ChatFormatting,Long>> entry: players.entrySet()){
                ChatColor color = ChatColor.valueOf(entry.getValue().getA().name());
                System.out.println(entry.getValue().getA().name()+" : "+ color.name());
                glow.addPlayerTime(entry.getKey(),color, entry.getValue().getB());
            }
            return glowMap.put(uid,glow);
        }
        Glow glow = new Glow(entity,players);
        glowMap.put(uid,glow);
        return glow;
    }

    private PlayerTeam createTeam(ChatFormatting color){
        if (color == null || !color.isColor()) throw new IllegalArgumentException("ChatFormatting must be a color " +
                "format");
        PlayerTeam team = new PlayerTeam(SCOREBOARD, glowEntity.getUniqueId().toString());
        team.setColor(color);
        return team;
    }

    public void addPlayer(Player player, ChatColor color) {
        addPlayerTime(player,color,-1L);
    }

    public void addPlayerTime(Player player, ChatColor color, Long time) {
        if (color == null || !color.isColor())
            throw new IllegalArgumentException("ChatColor must be a color format" + color);

        UUID ID = player.getUniqueId();
        ChatFormatting chatFormatting = ChatFormatting.getByName(color.name());
        Tuple<PlayerTeam,Integer> tuple = players.get(ID) == null ? new Tuple<>(null,-1) :
                players.get(ID);


        //si le joueur n'est pas dans la liste
        if (!players.containsKey(ID)){
            sendGlowPacket(player,true,glowEntity.getEntityId());
            if (time != -1) {
                tuple.setB(scheduler(player, time));
            }
        }
        else {
            //si le joueur est dans la liste mais que le temps n'est pas null
            if (tuple.getB() != -1) {
                Bukkit.getScheduler().cancelTask(tuple.getB());
                tuple.setB(scheduler(player, time));
            }
            if (chatFormatting == null) {
                players.put(ID, tuple);
                return;
            }
            //si le joueur est dans la liste mais que la couleur est différente
            if (tuple.getA().getColor() != chatFormatting) {
                if (players.containsKey(ID) && players.get(ID).getA() == null)
                    sendTeamPacket(player, createTeam(chatFormatting), true);
                ChangeColor(player, chatFormatting);
                return;
            }
        }
        tuple.setA(createTeam(chatFormatting));
        sendTeamPacket(player,tuple.getA(),true);
        players.put(ID, tuple);

    }

    public void addPlayers(HashMap<Player,ChatColor> players){
        for (Map.Entry<Player,ChatColor> entry: players.entrySet()){
            addPlayer(entry.getKey(),entry.getValue());
        }
    }

    public void addPlayersTime(HashMap<Player,Tuple<ChatColor,Long>> players){
        for (Map.Entry<Player,Tuple<ChatColor,Long>> entry: players.entrySet()){
            addPlayerTime(entry.getKey(),entry.getValue().getA(),entry.getValue().getB());
        }
    }

    public void removePlayer(Player player){
        UUID ID = player.getUniqueId();
        if (!players.containsKey(ID)) return;
        tempTeam = players.get(ID).getA();
        players.remove(ID);
        removeGlow(player);
        if (players.isEmpty())
            glowMap.remove(glowEntity.getEntityId());

    }

    public void removeGlow(){
        glowMap.remove(glowEntity.getEntityId());
        for (UUID player: players.keySet()){
            tempTeam = players.get(player).getA();
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

    private void addGlow(Player player,PlayerTeam team){
        sendGlowPacket(player,true,glowEntity.getEntityId());
        if (team == null) return;
        sendTeamPacket(player,team,true);
        sendPlayerTeamPacket(player,team, getEntityId(glowEntity), ClientboundSetPlayerTeamPacket.Action.ADD);
    }

    private void removeGlow(Player player){
        if (!player.hasPotionEffect(PotionEffectType.GLOWING))
            sendGlowPacket(player,false,glowEntity.getEntityId());
        if (tempTeam == null) return;
        removeTeam(player);
        tempTeam = null;
    }

    private void ChangeColor(Player player,ChatFormatting color){
        UUID ID = player.getUniqueId();
        Tuple<PlayerTeam,Integer> tuple = players.get(ID);
        PlayerTeam team = players.get(ID).getA();
        team.setColor(color);
        sendTeamPacket(player,team,false);
        tuple.setA(team);
        players.put(ID,tuple);
    }

    private void removeTeam(Player client){
        if (tempTeam == null) return;
        String uid = getEntityId(glowEntity);
        sendPlayerTeamPacket(client,tempTeam, uid, ClientboundSetPlayerTeamPacket.Action.REMOVE);
        sendTeamRemovePacket(client,tempTeam);

        PlayerTeam team =((CraftScoreboard) client.getScoreboard()).getHandle().getPlayersTeam(uid);
        if (team != null)
            sendPlayerTeamPacket(client,team, uid, ClientboundSetPlayerTeamPacket.Action.ADD);

    }


    //====================Packet====================

    public static ServerGamePacketListenerImpl getConnection(Player player){
        CraftPlayer craftPlayer = (CraftPlayer) player;
        if (craftPlayer == null) throw new IllegalArgumentException("Player must be online");
        return craftPlayer.getHandle().connection;
    }

    private static void sendGlowPacket(Player player,boolean glowing,int id){
        player.sendMessage("the entity "+id+" is "+(glowing ? "glowing" : "not glowing"));
        byte glowingByte = glowing ? 0x40 : (byte) 0;
        List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
        eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), glowingByte));;
        getConnection(player).send(new ClientboundSetEntityDataPacket(id, eData));
    }

    private static void sendTeamPacket(Player player, PlayerTeam team,boolean create) {
        player.sendMessage("the team "+team.getName()+" is "+(create ? "created" : "modified"));
        getConnection(player).send(createAddOrModifyPacket(team, create));
    }

    private static void sendTeamRemovePacket(Player player, PlayerTeam team) {
        player.sendMessage("the team "+team.getName()+" is removed");
        getConnection(player).send(createRemovePacket(team));
    }

    private static void sendPlayerTeamPacket(Player player1, PlayerTeam team, String entity,
                                        ClientboundSetPlayerTeamPacket.Action action) {
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
                ClientboundSetPlayerTeamPacket GlowPlayersNames = (ClientboundSetPlayerTeamPacket) event.getPacket().getHandle();
                String[] players = GlowPlayersNames.getPlayers().toArray(new String[0]);
                for (String player: players){
                    Player player1 = Bukkit.getPlayer(player);
                    int id = player1 == null? -1 : player1.getEntityId();
                    if (!glowMap.containsKey(id)) continue;
                    Glow glow = glowMap.get(id);
                    if (!glow.players.containsKey(event.getPlayer().getUniqueId()) ||
                            glow.players.get(event.getPlayer().getUniqueId()).getA() == null) continue;
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
                Tuple<PlayerTeam,Integer> temp = glow.players.remove(player.getUniqueId());
                PlayerTeam team = temp.getA();
                sendGlowPacket(player,true,entity);
                sendTeamPacket(player,team,true);
                sendPlayerTeamPacket(player,team,getEntityId(glow.glowEntity), ClientboundSetPlayerTeamPacket.Action.ADD);
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
