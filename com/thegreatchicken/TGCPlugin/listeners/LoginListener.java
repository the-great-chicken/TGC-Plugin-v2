package com.thegreatchicken.TGCPlugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.thegreatchicken.TGCPlugin.warp.DeferedWarp;

public class LoginListener implements Listener {
	
	public static class LoginDeferedWarp extends DeferedWarp {

		public LoginDeferedWarp(Player player, String warp) {
			super(player, warp);
		}
		
		@Override
		public void run () {
			Location a = player.getLocation();
			Location b = player.getWorld().getSpawnLocation();
			
			boolean sameWorld = a.getWorld().equals(b.getWorld());
			
			double dx = Math.abs(a.getX() - b.getX());
			double dy = Math.abs(a.getY() - b.getY());
			double dz = Math.abs(a.getZ() - b.getZ());
			
			double ds = dx + dy + dz;
			
			final double threshold = 5;
			
			boolean aboutSamePosition = ds <= threshold;
			boolean positionAreEquals = aboutSamePosition && sameWorld;
			
			if (positionAreEquals)
				this.runLater();
			else super.run();
		}
		
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin (PlayerLoginEvent event) {
		new LoginDeferedWarp(event.getPlayer(), "lobby")
		   .runLater();
	}
	
}
