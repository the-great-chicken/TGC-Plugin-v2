package com.thegreatchicken.TGCPlugin.glow;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowPacketListener implements PacketListener {


    @Override
    public void onPacketSend(PacketSendEvent event) {
        var cancel = switch (event.getPacketType()){
            case PacketType.Play.Server.TEAMS -> onTeamPacketSend(event.getPlayer(),new WrapperPlayServerTeams(event));
            case PacketType.Play.Server.ENTITY_METADATA -> {
                onGlowPacketSend(new WrapperPlayServerEntityMetadata(event));
                yield true;
            }
            default -> false;
        };
        event.setCancelled(cancel);

    }

    public boolean onTeamPacketSend(Player client,WrapperPlayServerTeams packet){
        String[] players = packet.getPlayers().toArray(new String[0]);
        boolean result = false;
        for (String player: players){
            Player player1 = Bukkit.getPlayer(player);
            int id = player1 == null? -1 : player1.getEntityId();
            if (!Glow.hasGlow(id)) continue;
            Glow glow = Glow.getGlowByEntityID(id);
            if (glow == null || player1 == null || !glow.hasGlow(player1.getUniqueId()) ||
                glow.getGlow(player1.getUniqueId()).team() == null) continue;
            client.sendMessage("team packet canceled");
            result = true;
        }
        return result;
    }


    public boolean onGlowPacketSend(WrapperPlayServerEntityMetadata packet){
        ClientboundSetEntityDataPacket GlowPacket =
            (ClientboundSetEntityDataPacket) packet.getPacket().getHandle();
        List<SynchedEntityData.DataValue<?>> edata = GlowPacket.packedItems();
        if(edata.contains(SynchedEntityData.DataValue.create(
            new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) 0x40))
            || edata.contains(SynchedEntityData.DataValue.create(
            new EntityDataAccessor<>(0,EntityDataSerializers.BYTE), (byte) 0))){
            Glow glow = getGlowByEntityID(GlowPacket.id());
            if (glow == null || !glow.players.containsKey(packet.getPlayer().getUniqueId())) return;
            packet.getPlayer().sendMessage("glow packet canceled");
            packet.setCancelled(true);
        }
    }
}
