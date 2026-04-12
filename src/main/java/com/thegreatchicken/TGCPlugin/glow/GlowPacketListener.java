package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.thegreatchicken.TGCPlugin.glow.Glow.getGlowByEntityID;

public class GlowPacketListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        switch (event.getPacketType()){
            case PacketType.Play.Server.TEAMS -> onTeamPacketSend(event);
            case PacketType.Play.Server.ENTITY_METADATA -> onGlowPacketSend(event);
            default -> {return;}
        };
    }

    public void onTeamPacketSend(PacketSendEvent event){
        var packet = new WrapperPlayServerTeams(event);
        Player client = event.getPlayer();
        String[] players = packet.getPlayers().toArray(new String[0]);
        boolean result = false;
        for (String player: players){
            Player player1 = Bukkit.getPlayer(player);
            int id = player1 == null? -1 : player1.getEntityId();
            if (!Glow.hasGlow(id)) continue;
            Glow glow = getGlowByEntityID(id);
            if (glow == null || player1 == null || !glow.seeGlow(player1.getUniqueId()) ||
                glow.getGlow(player1.getUniqueId()).team() == null) continue;
            client.sendMessage("team packet canceled");
            result = true;
        }
        event.setCancelled(result);
    }


    public void onGlowPacketSend(PacketSendEvent event){
        var packet = new WrapperPlayServerEntityMetadata(event);
        Player client = event.getPlayer();
        var entityMetadata = packet.getEntityMetadata();
        if(entityMetadata.stream().anyMatch(e-> e.getIndex() == 0
            && ((byte)e.getValue() == 0 || (byte)e.getValue() == 0x40))){
            Glow glow = getGlowByEntityID(packet.getEntityId());
            if (glow == null || !glow.seeGlow(client.getUniqueId())) return;
            event.setCancelled(true);
        }
    }
}
