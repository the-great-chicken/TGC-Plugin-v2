package com.thegreatchicken.TGCPlugin.warp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.warp.Warp;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;

public class CreateWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		
		if (args.length != 7) {
			sender.sendMessage("Expected 7 arguments, got " + args.length);
			return false;
		}
		
		String name = args[0];
		String w_nm = args[1];
		float x, y, z, yaw, pitch;
		try {
			x = Float.parseFloat(args[2]);
			y = Float.parseFloat(args[3]);
			z = Float.parseFloat(args[4]);
			
			yaw   = Float.parseFloat(args[5]);
			pitch = Float.parseFloat(args[6]);
		} catch (Exception e) {
			sender.sendMessage("Expected floats for x, y, z, yaw, pitch coordinates");
			return false;
		}
		
		Warp warp = new Warp(name, w_nm, x, y, z, yaw, pitch);
		
		WarpManager.append(warp);
		
		sender.sendMessage("Successfully created warp");
		
		return true;
	}
	
}
