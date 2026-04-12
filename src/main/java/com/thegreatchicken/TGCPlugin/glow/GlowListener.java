package com.thegreatchicken.TGCPlugin.glow;

import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.thegreatchicken.TGCPlugin.glow.Glow.*;
import static com.thegreatchicken.TGCPlugin.glow.PacketUtils.*;

public class GlowListener implements Listener {

    @EventHandler
    public static void onPlayerJoin(PlayerClientLoadedWorldEvent event){
        Player player = event.getPlayer();
        loadGlow(player);
    }

    public static void loadGlow(Player player){
        player.sendMessage("load glow");
        getGlowEntities().forEach(entity -> {
            Glow glow = Glow.getGlowByEntityID(entity);
            if (glow == null){return;}
            if (glow.seeGlow(player.getUniqueId())){
                GlowInstance team = glow.getGlow(player.getUniqueId());
                sendGlowPacket(player,true,entity);
                sendTeamCreatePacket(player,team,true);
                sendTeamJoinLeavePacket(player,team,getEntityId(glow.getGlowEntity()), true);
            }
        });
    }
}
