package com.thegreatchicken.TGCPlugin.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class InventoryListener implements Listener {
	
	@EventHandler
	public void onDrop (PlayerDropItemEvent event) {
		InventoryManager.onDrop(event);
	}
	@EventHandler
	public void onPickup (PlayerPickupItemEvent event) {
		InventoryManager.onPickup(event);
	}
	@EventHandler
	public void onInventoryInteraction (InventoryClickEvent event) {
		InventoryManager.onChange(event);
	}
	@EventHandler
	public void onDrag (InventoryDragEvent event) {
		InventoryManager.onDrag(event);
	}
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		InventoryManager.onPlayerUse(event);
	}

}
