package com.thegreatchicken.TGCPlugin.inventory;

import java.util.HashMap;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.thegreatchicken.TGCPlugin.inventory.maintainers.LobbyInventory;
import com.thegreatchicken.TGCPlugin.inventory.maintainers.PvpInventory;

public class InventoryManager {

	public static HashMap<Player, InventoryMaintainer> maintainers = new HashMap<Player, InventoryMaintainer>(); 
	public static InventoryMaintainer getMaintainer (Player player) {
		return maintainers.getOrDefault(player, null);
	}
	public static InventoryMaintainer getMaintainerOrDefault (Player player) {
		return maintainers.getOrDefault(player, new InventoryMaintainer());
	}
	public static void useMaintainer (Player player, InventoryMaintainer maintainer) {
		maintainers.put(player, maintainer);
		
		maintainer.onLoad(player);
	}
	public static void onWarp (Player player, String warp) {
		if (warp.equals("lobby"))
			useMaintainer(player, new LobbyInventory());
		if (warp.equals("pvp"))
			useMaintainer(player, new PvpInventory());
	}
	
	public static void onDrop (PlayerDropItemEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());
		
		if (maintainer == null) return ;
		maintainer.onDrop(event);
	}
	public static void onPickup (PlayerPickupItemEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());
		
		if (maintainer == null) return ;
		maintainer.onPickup(event);
	}
	public static void onChange (InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) return ;
		InventoryMaintainer maintainer = getMaintainer((Player) entity);
		
		if (maintainer == null) return ;
		maintainer.onChange(event);
	}
	public static void onDrag (InventoryDragEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) return ;
		InventoryMaintainer maintainer = getMaintainer((Player) entity);
		
		if (maintainer == null) return ;
		maintainer.onDrag(event);
	}
	public static void onPlayerUse(PlayerInteractEvent event) {
		InventoryMaintainer maintainer = getMaintainer(event.getPlayer());
		
		if (maintainer == null) return ;
		maintainer.onPlayerUse(event);
	}
	
}
