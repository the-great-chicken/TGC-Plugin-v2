package com.thegreatchicken.TGCPlugin.inventory;

import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class InventoryValidator implements Runnable {

	public void runLater () {
		PluginLoader.BUKKIT_SERVER
			.getScheduler()
			.runTaskLater(PluginLoader.PLUGIN, this, 1);
	}
	
	@Override
	public void run() {
		for (Entry<Player, InventoryMaintainer> entry : InventoryManager.maintainers.entrySet())
			entry.getValue().onTick(entry.getKey());
		
		this.runLater();
	}
	
}
