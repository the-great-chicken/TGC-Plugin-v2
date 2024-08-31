package com.thegreatchicken.TGCPlugin.glow.commands;

import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.glow.containers.GlowingMaintainer;
import com.thegreatchicken.TGCPlugin.utils.Selector;

public class RemoveGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length == 0) {
			GlowingMaintainer.instance().clear();
			return true;
		}
		
		Collection<Player> players = Selector.selectPlayers(sender, args[0]);

		for (Player player : players) GlowingMaintainer.instance().removeGlow(player);
			
		return true;
	}

	

}
