package com.thegreatchicken.TGCPlugin.glow.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.glow.GlowManager;

public class UseGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		if (args.length == 1) {
			if (sender.isOp() && args[0].equals("toggle")) {
				GlowManager.USE_GLOW_ENABLED = !GlowManager.USE_GLOW_ENABLED;
				return true;
			} else {
				return false;
			}
		}
		
		GlowManager.useGlow((Player) sender);
		
		return true;
	}

}
