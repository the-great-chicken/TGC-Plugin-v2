package com.thegreatchicken.TGCPlugin.warp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.warp.Warp;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;

public class StatusWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		
		if (args.length != 2) {
			sender.sendMessage("Expected 2 arguments, got " + args.length);
			return false;
		}
		if (!(args[1].equals("disabled") || args[1].equals("enabled"))) {
			sender.sendMessage("Could not recognize status " + args[1]);
			return false;
		}
		
		Warp warp = WarpManager.get(args[0]);
		if (warp == null) {
			sender.sendMessage("No such warp.");
			return false;
		}
		warp.enabled = args[1].equals("enabled");
		
		sender.sendMessage("Successfully set warp status");
		
		return true;
	}

}
