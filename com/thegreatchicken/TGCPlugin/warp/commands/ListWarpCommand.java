package com.thegreatchicken.TGCPlugin.warp.commands;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.warp.Warp;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;

public class ListWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		ArrayList<String> names = new ArrayList<>();
		
		for (Warp warp : WarpManager.get())
			if (warp.enabled)
				names.add(warp.name);
		
		if (names.size() == 0) sender.sendMessage("There are no warps.");
		else {
			String message = "Warp list : ";
			
			for (int i = 0; i < names.size(); i ++) {
				message += names.get(i);
				if (i + 1 != names.size())
					message += ", ";
			}
			
			sender.sendMessage(message);
		}
		
		return true;
	}

}
