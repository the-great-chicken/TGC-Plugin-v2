package com.thegreatchicken.TGCPlugin.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.inventory.InventoryManager;

public class ClearPreprocessor implements Listener {

	public boolean isClearCommand (String label) {
		if (label.length() != 0 && label.charAt(0) == '/')
			label = label.substring(1);
		
		return label.startsWith("clear")
			|| label.startsWith("minecraft:clear");
	}
	
	public static List<Entity> get (BlockCommandSender sender, String selector) {
		Player byName = PluginLoader.BUKKIT_SERVER.getPlayer(selector);
		if (byName == null)
			return Bukkit.selectEntities(sender, selector);
	
		ArrayList<Entity> array = new ArrayList<>();
		array.add(byName);
		
		return array;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)	
    public void onCommandBlockDispatch(ServerCommandEvent event) {
		if (!(event.getSender() instanceof BlockCommandSender)) return ;
		
		BlockCommandSender sender = (BlockCommandSender) event.getSender();
		
		String command = event.getCommand();
        String[] words = command.split(" ");
        if (!isClearCommand(words[0])) return ;
        
        if (words.length == 1) {
            event.setCancelled(true);
        	return ;
        }
        
        String selector = command.substring(command.indexOf(' ') + 1);
        
        List<Entity> entities = get(sender, selector);
        
        for (Entity entity : entities) {
        	if (entity == null || (!(entity instanceof Player))) continue ;
        	
        	Player player = (Player) entity;
			System.out.println("Applying clear to " + player.getName());
        	
        	InventoryManager.getMaintainerOrDefault(player)
        					.onClear(player);
        }

        event.setCancelled(true);
        event.setCommand  ("");
    }
	
}
