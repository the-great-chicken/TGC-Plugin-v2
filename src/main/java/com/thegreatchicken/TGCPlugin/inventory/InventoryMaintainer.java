package com.thegreatchicken.TGCPlugin.inventory;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class InventoryMaintainer{
	
	public void onDrop (PlayerDropItemEvent event) {  }
	public void onPickup (PlayerPickupItemEvent event) {  }
	public void onChange (InventoryClickEvent event) {  }
	public void onDrag (InventoryDragEvent event) {  }
	public void onPlayerUse(PlayerInteractEvent event) {  }
	public void onItemChange(PlayerInventorySlotChangeEvent event) {  }
	public void onHandSwitch(PlayerSwapHandItemsEvent event) {  }

	public void onLoad (Player player) {  }
	public void onTick (Player player) {  }
	public void onClear (Player player) {
		player.getInventory().clear();
		
		onTick(player);
	}
	
}
