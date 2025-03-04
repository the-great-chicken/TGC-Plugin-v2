package com.thegreatchicken.TGCPlugin.glow;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.thegreatchicken.TGCPlugin.glow.Glow.loadGlow;

public class GlowListener implements Listener {

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(PluginLoader.PLUGIN,() -> loadGlow(player),15);
    }
}
