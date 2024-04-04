package com.thegreatchicken.TGCPlugin.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class InventoryMaintainer implements Listener {
	
	@EventHandler
	public void onDrop (PlayerDropItemEvent event) {  }
	@EventHandler
	public void onPickup (PlayerPickupItemEvent event) {  }
	@EventHandler
	public void onChange (InventoryClickEvent event) {  }
	@EventHandler
	public void onDrag (InventoryDragEvent event) {  }
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {  }
	
	public void onLoad (Player player) {  }
	public void onTick (Player player) {  }
	public void onClear (Player player) {
		player.getInventory().clear();
		
		onTick(player);
	}
	
}
