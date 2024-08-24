package com.thegreatchicken.TGCPlugin.glow.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.glow.GlowAction;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;

public class RemoveGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 1) return false;
		
		
		String name = args[0];
		for (GlowAction action : GlowManager.pendingActions) {
			if (action.entity.getName().equals(name))
				action.closed = true;
		}
			
		return true;
	}

}
