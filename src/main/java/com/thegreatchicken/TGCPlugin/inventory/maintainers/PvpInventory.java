package com.thegreatchicken.TGCPlugin.inventory.maintainers;

import java.util.Set;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.thegreatchicken.TGCPlugin.inventory.InventoryMaintainer;

public class PvpInventory extends InventoryMaintainer {

	private final static ItemStack OSLP =  new ItemStack(Material.DRAGON_BREATH, 1);
	static {
		ItemMeta meta = OSLP.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.AQUA + "Où sont les poulets ?");
		OSLP.setItemMeta(meta);
	};


	public void onDrop (PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType() != Material.DRAGON_BREATH) return ;
		event.getItemDrop().remove();
		event.setCancelled(true);
		event.getPlayer().closeInventory();
	}

	public void onChange (InventoryClickEvent event) {
		if (event.getSlot() != 8) return ;
		if (!(event.getInventory() instanceof PlayerInventory) && !(event.getInventory() instanceof CraftingInventory)) return ;
		event.setCancelled(true);
		event.getWhoClicked().closeInventory();
	}
	public void onDrag (InventoryDragEvent event) {
		Set<Integer> slots = event.getInventorySlots();
		if (!(event.getInventory() instanceof PlayerInventory)) return ;
		if (!slots.contains(8)) return ;
		
		event.setCancelled(true);
	}

	public void onItemChange(PlayerInventorySlotChangeEvent event) {
		if (event.getSlot() == 8){
			event.getPlayer().getInventory().setItem(8,OSLP);
		}
	}
	
	public void onPlayerUse(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null || !event.getAction().isRightClick()) return ;
		
		Material material = item.getType();
		if (material != Material.DRAGON_BREATH) return ;
		Player player = event.getPlayer();
		player.performCommand("useglow");
	}
	
	public void onHandSwitch(PlayerSwapHandItemsEvent event) {
		if (event.getOffHandItem().getType() != Material.DRAGON_BREATH) return ;
		event.setCancelled(true);
	}

	public void onLoad (Player player) {
		player.getInventory().setItem(8, OSLP);
	}

}
