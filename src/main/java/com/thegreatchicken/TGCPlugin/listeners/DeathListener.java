package com.thegreatchicken.TGCPlugin.listeners;

import com.thegreatchicken.TGCPlugin.glow.Glow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        Glow.removeGlow(player);
    }
}
