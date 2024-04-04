package com.thegreatchicken.TGCPlugin.warp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;

public class UseWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!label.equals("usewarp")) return false;
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only a player can use this command");
			return false;
		}
		
		if (args.length != 1) {
			sender.sendMessage("Only one argument for warp name expected");
			return false;
		}
		
		String warp_name = args[0];
		Player player    = (Player) sender;
		
		WarpManager.warp(player, warp_name);
		
		return true;
	}

}
