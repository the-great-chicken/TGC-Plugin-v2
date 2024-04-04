package com.thegreatchicken.TGCPlugin.warp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.warp.Warp;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;

public class RemoveWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		
		if (args.length != 1) {
			sender.sendMessage("Expected 1 argument, got " + args.length);
			return false;
		}
		
		Warp data = WarpManager.remove(args[0]);
		
		if (data == null) {
			sender.sendMessage("No such warp.");
		} else {
			sender.sendMessage("Removed warp in world " + data.world_name + " at position " + data.x + " " + data.y + " " + data.z);
		}
		
		return true;
	}

}
