package com.thegreatchicken.TGCPlugin.warp;

import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class DeferedWarp implements Runnable {
	
	public Player player;
	public String warp;
	
	public long delay = 1;
	
	public DeferedWarp (Player player, String warp) {
		this.player = player;
		this.warp   = warp;
	}
	public DeferedWarp (Player player, String warp, long delay) {
		this(player, warp);
		
		this.delay = delay;
	}

	@Override
	public void run() {
		System.out.println(player.getLocation());
		player.performCommand("usewarp " + this.warp);
	}
	
	public void runLater () {
		PluginLoader.BUKKIT_SERVER
			.getScheduler()
			.runTaskLater(PluginLoader.PLUGIN, this, delay);
	}
	
}
