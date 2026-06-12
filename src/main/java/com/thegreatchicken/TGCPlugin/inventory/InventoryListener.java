package com.thegreatchicken.TGCPlugin.inventory;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import static com.thegreatchicken.TGCPlugin.inventory.InventoryManager.getMaintainer;

public class InventoryListener implements Listener {
	
	@EventHandler
	public void onDrop (PlayerDropItemEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());

		if (maintainer == null) return ;
		maintainer.onDrop(event);
	}
	@EventHandler
	public void onPickup (PlayerPickupItemEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());

		if (maintainer == null) return ;
		maintainer.onPickup(event);
	}
	@EventHandler
	public void onInventoryInteraction (InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) return ;
		InventoryMaintainer maintainer = getMaintainer((Player) entity);

		if (maintainer == null) return ;
		maintainer.onChange(event);
	}
	@EventHandler
	public void onDrag (InventoryDragEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) return ;
		InventoryMaintainer maintainer = getMaintainer((Player) entity);

		if (maintainer == null) return ;
		maintainer.onDrag(event);
	}
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());

		if (maintainer == null) return ;
		maintainer.onPlayerUse(event);
	}

	@EventHandler
	public void onItemChange(PlayerInventorySlotChangeEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());
		if (maintainer == null) return ;
		maintainer.onItemChange(event);
	}
	@EventHandler
	public void onHandSwitch(PlayerSwapHandItemsEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());
		if (maintainer == null) return ;
		maintainer.onHandSwitch(event);
	}

}
