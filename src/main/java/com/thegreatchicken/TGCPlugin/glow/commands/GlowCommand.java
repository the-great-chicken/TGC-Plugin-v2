package com.thegreatchicken.TGCPlugin.glow.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;

public class GlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 2) return false;
		
		String playerName = args[0];
		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage("Expected integer as second argument");
			return false;
		}
		
		Player source = PluginLoader.BUKKIT_SERVER.getPlayer(playerName);
		if (source == null) {
			sender.sendMessage("Expected online player");
			return false;
		}
		
		for (Player player : PluginLoader.BUKKIT_SERVER.getOnlinePlayers())
			GlowManager.sendPotionEffect(player, source, duration, "WHITE");
		
		return true;
	}

}
