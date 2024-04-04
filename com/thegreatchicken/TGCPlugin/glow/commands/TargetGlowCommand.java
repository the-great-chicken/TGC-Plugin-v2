package com.thegreatchicken.TGCPlugin.glow.commands;

import java.util.List;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import com.thegreatchicken.TGCPlugin.listeners.ClearPreprocessor;

public class TargetGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 2 && args.length != 3) return false;
		
		String color = "WHITE";
		if (args.length == 3) color = args[2];
		
		if (sender instanceof BlockCommandSender) {
			List<Entity> client = ClearPreprocessor.get((BlockCommandSender) sender, args[0]);
			List<Entity> entity = ClearPreprocessor.get((BlockCommandSender) sender, args[1]);
			
			for (Entity cl : client) {
				if (cl == null || !(cl instanceof Player)) continue ;
				for (Entity en : entity)
					if (en != null && en instanceof Player) 
						GlowManager.sendPotionEffect((Player) cl, (Player) en, 1000000, color);
			}
		} else {
			Player client = PluginLoader.BUKKIT_SERVER.getPlayer(args[0]);
			Player entity = PluginLoader.BUKKIT_SERVER.getPlayer(args[1]);
		
			if (client == null || entity == null) {
				sender.sendMessage("Could not find players");
				return true;
			}

			GlowManager.sendPotionEffect((Player) client, (Player) entity, 1000000, color);
		}
		
		return true;
	}

}
