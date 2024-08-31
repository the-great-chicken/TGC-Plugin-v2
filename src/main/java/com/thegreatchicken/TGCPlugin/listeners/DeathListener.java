package com.thegreatchicken.TGCPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.thegreatchicken.TGCPlugin.glow.containers.GlowingMaintainer;

public class DeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        
        GlowingMaintainer.instance().removeGlow(player);
    }
}
