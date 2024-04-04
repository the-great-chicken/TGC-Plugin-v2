package com.thegreatchicken.TGCPlugin.inventory.maintainers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.thegreatchicken.TGCPlugin.inventory.InventoryMaintainer;
import com.thegreatchicken.TGCPlugin.warp.DeferedWarp;

public class LobbyInventory extends InventoryMaintainer {

	@EventHandler
	public void onDrop (PlayerDropItemEvent event) {  }
	@EventHandler
	public void onPickup (PlayerPickupItemEvent event) {  }
	@EventHandler
	public void onChange (InventoryClickEvent event) {
		event.setCancelled(true);
		event.getWhoClicked().closeInventory();
	}
	@EventHandler
	public void onDrag (InventoryDragEvent event) {
		event.setCancelled(true);
		event.getWhoClicked().closeInventory();
	}
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null) return ;
		
		Material material = item.getType();
		if (material != Material.COMPASS) return ;
	
		Player player = event.getPlayer();
		
		new DeferedWarp(event.getPlayer(), "pvp")
		   .runLater();
	}

	public void onLoad (Player player) {
		ItemStack item = new ItemStack(Material.COMPASS, 1);
		ItemMeta  meta = item.getItemMeta();
		
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.AQUA + "Se téléporter dans l'" + ChatColor.GOLD + "" + ChatColor.BOLD + "Arène");
		item.setItemMeta(meta);
		
		player.getInventory().setItem(4, item);
	}
	public void onTick (Player player) {
		ItemStack compass = player.getInventory().getItem(4);
		if (compass == null || compass.getType() != Material.COMPASS)
			onLoad(player);
	}
	
}
