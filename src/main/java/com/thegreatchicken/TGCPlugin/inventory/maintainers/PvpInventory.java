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


	@EventHandler
	public void onDrop (PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType() != Material.DRAGON_BREATH) return ;
		event.getItemDrop().remove();
		event.setCancelled(true);
		event.getPlayer().closeInventory();
	}
	@EventHandler
	public void onChange (InventoryClickEvent event) {
		if (event.getSlot() != 8) return ;
		if (!(event.getInventory() instanceof PlayerInventory) && !(event.getInventory() instanceof CraftingInventory)) return ;
		event.setCancelled(true);
		event.getWhoClicked().closeInventory();
	}
	@EventHandler
	public void onDrag (InventoryDragEvent event) {
		Set<Integer> slots = event.getInventorySlots();
		if (!(event.getInventory() instanceof PlayerInventory)) return ;
		if (!slots.contains(8)) return ;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryChange(PlayerInventorySlotChangeEvent event) {
		if (event.getSlot() == 8){
			event.getPlayer().getInventory().setItem(8,OSLP);
		}
	}
	
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null || !event.getAction().isRightClick()) return ;
		
		Material material = item.getType();
		if (material != Material.DRAGON_BREATH) return ;
		Player player = event.getPlayer();
		player.performCommand("useglow");
	}
	
	@EventHandler
	public void onItemSwap(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		if (player.getInventory().getItem(8).getType() == Material.DRAGON_BREATH
		 && player.getInventory().getItem(8).getItemMeta().getDisplayName().equals(
			ChatColor.RESET + "" + ChatColor.AQUA + "Où sont les poulets ?"
		 )) return ;

		event.setCancelled(true);
	}

	public void onLoad (Player player) {

		
		player.getInventory().setItem(8, OSLP);
	}
	public void onTick (Player player) {
		ItemStack compass = player.getInventory().getItem(8);
		if (compass == null || compass.getType() != Material.DRAGON_BREATH)
			onLoad(player);
		
		compass = player.getInventory().getItem(8);
		
		/*boolean enchanted = !GlowManager.isInCooldown(player);
		ItemMeta meta = compass.getItemMeta();
		if (enchanted) {
			Map<Enchantment, Integer> enchants = meta.getEnchants();
			
			if (enchants.size() == 0) {
				GlowEnchantment enchant = new GlowEnchantment();
				
				meta.addEnchant(enchant, 1, true);
			}
		} else if (!enchanted) {
			Map<Enchantment, Integer> enchants = meta.getEnchants();
			
			for (Enchantment enchant : enchants.keySet())
				meta.removeEnchant(enchant);
		}*/
		//compass.setItemMeta(meta);
	}

}
