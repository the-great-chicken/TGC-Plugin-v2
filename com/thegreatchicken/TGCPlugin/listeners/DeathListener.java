package com.thegreatchicken.TGCPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.thegreatchicken.TGCPlugin.glow.GlowAction;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;

public class DeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        
        for (GlowAction action : GlowManager.pendingActions)
			if (action.entity == player)
				action.closed = true;
    }
}
